package com.shakerlab.app.features.catalog.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.usecase.cocktail.FilterByCategoryUseCase
import com.shakerlab.app.domain.usecase.cocktail.GetCategoriesUseCase
import com.shakerlab.app.domain.usecase.cocktail.GetRandomCocktailUseCase
import com.shakerlab.app.domain.usecase.favorites.GetFavoritesUseCase
import com.shakerlab.app.domain.usecase.favorites.ToggleFavoriteUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout

class CatalogViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val filterByCategoryUseCase: FilterByCategoryUseCase,
    private val getRandomCocktailUseCase: GetRandomCocktailUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _cocktails = MutableLiveData<List<CocktailPreview>>()
    val cocktails: LiveData<List<CocktailPreview>> = _cocktails

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _randomId = MutableLiveData<String?>()
    val randomId: LiveData<String?> = _randomId

    val favoriteIds: LiveData<Set<String>> = getFavoritesUseCase().map { list ->
        list.map { it.id }.toSet()
    }

    private var currentCategory = ""
    private val seen = mutableSetOf<String>()
    private var categoryJob: Job? = null

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val cats = getCategoriesUseCase()
                _categories.value = cats
                if (cats.isNotEmpty()) loadByCategory(cats.first())
            } catch (_: Exception) { }
        }
    }

    fun loadByCategory(category: String) {
        if (category == currentCategory) return
        currentCategory = category
        seen.clear()
        categoryJob?.cancel()
        categoryJob = viewModelScope.launch {
            // Debounce: ignore rapid taps on different category chips
            delay(150)
            _isLoading.value = true
            _error.value = null
            try {
                val cocktails = filterByCategoryUseCase(category)
                cocktails.forEach { seen.add(it.id) }
                _cocktails.value = cocktails
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _error.value = "Не удалось загрузить коктейли"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoading.value == true || _isLoadingMore.value == true) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val newItems = supervisorScope {
                    (1..6).map {
                        async {
                            try { withTimeout(5_000) { getRandomCocktailUseCase() } }
                            catch (_: Exception) { null }
                        }
                    }.awaitAll().filterNotNull()
                }
                    .filter { seen.add(it.id) }
                    .map { CocktailPreview(it.id, it.name, it.thumbnail, it.category, it.isAlcoholic) }
                if (newItems.isNotEmpty()) {
                    _cocktails.value = (_cocktails.value ?: emptyList()) + newItems
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun getRandom() {
        viewModelScope.launch {
            try {
                val cocktail = getRandomCocktailUseCase()
                _randomId.value = cocktail.id
            } catch (_: Exception) { }
        }
    }

    fun onRandomNavigated() {
        _randomId.value = null
    }

    fun toggleFavorite(preview: CocktailPreview) {
        viewModelScope.launch {
            toggleFavoriteUseCase(preview, favoriteIds.value ?: emptySet())
        }
    }
}
