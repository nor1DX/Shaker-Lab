package com.shakerlab.app.features.mybar.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.usecase.bar.AddBarIngredientUseCase
import com.shakerlab.app.domain.usecase.bar.GetBarIngredientsUseCase
import com.shakerlab.app.domain.usecase.bar.RemoveBarIngredientUseCase
import com.shakerlab.app.domain.usecase.cocktail.GetAllIngredientsUseCase
import com.shakerlab.app.domain.usecase.cocktail.GetRandomCocktailUseCase
import com.shakerlab.app.domain.usecase.favorites.GetFavoritesUseCase
import com.shakerlab.app.domain.usecase.favorites.ToggleFavoriteUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout

class MyBarViewModel(
    getBarIngredientsUseCase: GetBarIngredientsUseCase,
    private val addBarIngredientUseCase: AddBarIngredientUseCase,
    private val removeBarIngredientUseCase: RemoveBarIngredientUseCase,
    private val getRandomCocktailUseCase: GetRandomCocktailUseCase,
    private val getAllIngredientsUseCase: GetAllIngredientsUseCase,
    getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    val barIngredients: LiveData<List<String>> = getBarIngredientsUseCase()

    private val _cocktails = MutableLiveData<List<CocktailPreview>>(emptyList())
    private val _filter = MutableLiveData("All")

    val cocktails: LiveData<List<CocktailPreview>> = MediatorLiveData<List<CocktailPreview>>().also { med ->
        fun update() {
            val list = _cocktails.value ?: emptyList()
            med.value = when (_filter.value) {
                "Alcoholic" -> list.filter { it.isAlcoholic }
                "Non-Alcoholic" -> list.filter { !it.isAlcoholic }
                else -> list
            }
        }
        med.addSource(_cocktails) { update() }
        med.addSource(_filter) { update() }
    }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _allIngredients = MutableLiveData<List<String>>(emptyList())
    val allIngredients: LiveData<List<String>> = _allIngredients

    val favoriteIds: LiveData<Set<String>> = getFavoritesUseCase().map { list ->
        list.map { it.id }.toSet()
    }

    private var fetchJob: Job? = null
    private val seen = mutableSetOf<String>()

    private val ingredientsObserver = Observer<List<String>> { list ->
        fetchMatchingCocktails(list)
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

    private fun fetchMatchingCocktails(ingredients: List<String>) {
        fetchJob?.cancel()
        seen.clear()
        if (ingredients.isEmpty()) {
            _cocktails.value = emptyList()
            return
        }
        fetchJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = mutableListOf<CocktailPreview>()
                var attempts = 0
                while (results.size < 10 && attempts < 5) {
                    results += fetchPage(ingredients)
                    attempts++
                }
                _cocktails.value = results
            } catch (_: Exception) {
                _cocktails.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoading.value == true) return
        val ingredients = barIngredients.value ?: return
        if (ingredients.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val more = fetchWithRetry(ingredients)
                if (more.isNotEmpty()) {
                    _cocktails.value = (_cocktails.value ?: emptyList()) + more
                }
            } catch (_: Exception) { }
            finally { _isLoading.value = false }
        }
    }

    private suspend fun fetchWithRetry(ingredients: List<String>, maxAttempts: Int = 3): List<CocktailPreview> {
        repeat(maxAttempts) {
            val result = fetchPage(ingredients)
            if (result.isNotEmpty()) return result
        }
        return emptyList()
    }

    // Free CocktailDB API (v1) returns only 1 result per filterByIngredient call,
    // so we use getRandom() which returns full ingredient lists and filter locally.
    private suspend fun fetchPage(barIngredients: List<String>): List<CocktailPreview> {
        val normalized = barIngredients.map { it.lowercase() }.toSet()
        return supervisorScope {
            (1..40).map {
                async {
                    try { withTimeout(8_000) { getRandomCocktailUseCase() } }
                    catch (_: Exception) { null }
                }
            }.awaitAll().filterNotNull()
        }
            .filter { cocktail ->
                cocktail.id !in seen &&
                cocktail.ingredients.any { it.name.lowercase() in normalized }
            }
            .onEach { seen.add(it.id) }
            .map { CocktailPreview(it.id, it.name, it.thumbnail, it.category, it.isAlcoholic) }
    }

    fun setFilter(filter: String) {
        _filter.value = filter
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
