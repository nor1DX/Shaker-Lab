package com.shakerlab.app.features.mybar.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.usecase.bar.AddBarIngredientUseCase
import com.shakerlab.app.domain.usecase.bar.GetBarIngredientsUseCase
import com.shakerlab.app.domain.usecase.bar.RemoveBarIngredientUseCase
import com.shakerlab.app.domain.usecase.cocktail.FilterByIngredientUseCase
import com.shakerlab.app.domain.usecase.cocktail.GetAllIngredientsUseCase
import com.shakerlab.app.domain.usecase.favorites.GetFavoritesUseCase
import com.shakerlab.app.domain.usecase.favorites.ToggleFavoriteUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class MyBarViewModel(
    getBarIngredientsUseCase: GetBarIngredientsUseCase,
    private val addBarIngredientUseCase: AddBarIngredientUseCase,
    private val removeBarIngredientUseCase: RemoveBarIngredientUseCase,
    private val filterByIngredientUseCase: FilterByIngredientUseCase,
    private val getAllIngredientsUseCase: GetAllIngredientsUseCase,
    getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    val barIngredients: LiveData<List<String>> = getBarIngredientsUseCase()

    private val _cocktails = MutableLiveData<List<CocktailPreview>>(emptyList())
    val cocktails: LiveData<List<CocktailPreview>> = _cocktails

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _allIngredients = MutableLiveData<List<String>>(emptyList())
    val allIngredients: LiveData<List<String>> = _allIngredients

    val favoriteIds: LiveData<Set<String>> = getFavoritesUseCase().map { list ->
        list.map { it.id }.toSet()
    }

    private var fetchJob: Job? = null

    private val ingredientsObserver = Observer<List<String>> { list ->
        scheduleFetch(list)
    }

    init {
        loadAllIngredients()
        barIngredients.observeForever(ingredientsObserver)
    }

    private fun loadAllIngredients() {
        viewModelScope.launch {
            try { _allIngredients.value = getAllIngredientsUseCase() }
            catch (_: Exception) { }
        }
    }

    // Debounce: cancel previous and wait 400ms before actually fetching.
    // Prevents cascade of requests when user adds multiple ingredients quickly.
    private fun scheduleFetch(ingredients: List<String>) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            if (ingredients.isEmpty()) {
                _cocktails.value = emptyList()
                return@launch
            }
            delay(400)
            _isLoading.value = true
            try {
                val results = supervisorScope {
                    ingredients.map { ingredient ->
                        async { fetchForIngredient(ingredient) }
                    }.awaitAll()
                }
                    .flatten()
                    .distinctBy { it.id }
                    .shuffled()
                _cocktails.value = results
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _cocktails.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Search by full ingredient name; if < 5 results, also search by individual words.
    // "Absolut Vodka" → primary returns 1 → fallback "Vodka" → many more cocktails.
    // CancellationException is re-thrown so job cancellation propagates correctly.
    private suspend fun fetchForIngredient(ingredient: String): List<CocktailPreview> {
        val combined = mutableListOf<CocktailPreview>()

        try {
            combined.addAll(filterByIngredientUseCase(ingredient))
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {}

        if (combined.size < 5) {
            val words = ingredient.split(" ")
                .filter { it.length > 2 && !it.equals(ingredient, ignoreCase = true) }
            for (word in words) {
                try {
                    combined.addAll(filterByIngredientUseCase(word))
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {}
                if (combined.distinctBy { it.id }.size >= 5) break
            }
        }

        return combined.distinctBy { it.id }
    }

    fun toggleFavorite(preview: CocktailPreview) {
        viewModelScope.launch {
            toggleFavoriteUseCase(preview, favoriteIds.value ?: emptySet())
        }
    }

    fun addIngredient(name: String) {
        viewModelScope.launch { addBarIngredientUseCase(name.trim()) }
    }

    fun removeIngredient(name: String) {
        viewModelScope.launch { removeBarIngredientUseCase(name) }
    }

    override fun onCleared() {
        super.onCleared()
        barIngredients.removeObserver(ingredientsObserver)
    }
}
