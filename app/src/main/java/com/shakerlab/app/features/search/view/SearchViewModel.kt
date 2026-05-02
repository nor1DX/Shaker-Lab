package com.shakerlab.app.features.search.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.RecentSearchRepository
import com.shakerlab.app.domain.usecase.cocktail.SearchCocktailsUseCase
import com.shakerlab.app.domain.usecase.favorites.GetFavoritesUseCase
import com.shakerlab.app.domain.usecase.favorites.ToggleFavoriteUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchCocktailsUseCase: SearchCocktailsUseCase,
    getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val recentSearchRepository: RecentSearchRepository
) : ViewModel() {

    private val allResults = MutableLiveData<List<CocktailPreview>>(emptyList())
    private val _results = MutableLiveData<List<CocktailPreview>>(emptyList())
    val results: LiveData<List<CocktailPreview>> = _results

    private val _recentSearches = MutableLiveData<List<String>>()
    val recentSearches: LiveData<List<String>> = _recentSearches

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _isEmpty = MutableLiveData(false)
    val isEmpty: LiveData<Boolean> = _isEmpty

    val favoriteIds: LiveData<Set<String>> = getFavoritesUseCase().map { list ->
        list.map { it.id }.toSet()
    }

    private var searchJob: Job? = null
    private var isSearchActive = false
    var alcoholicFilter = "All"
        private set

    init { loadRecentSearches() }

    fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        searchJob?.cancel()
        isSearchActive = true
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isEmpty.value = false
            try {
                val cocktails = searchCocktailsUseCase(trimmed)
                allResults.value = cocktails
                applyFilter()
                _isEmpty.value = _results.value.isNullOrEmpty()
                saveRecentSearch(trimmed)
            } catch (_: Exception) {
                _error.value = "Search error"
                _results.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setAlcoholicFilter(filter: String) {
        alcoholicFilter = filter
        applyFilter()
    }

    private fun applyFilter() {
        val all = allResults.value ?: emptyList()
        _results.value = when (alcoholicFilter) {
            "Alcoholic" -> all.filter { it.isAlcoholic }
            "Non-alcoholic" -> all.filter { !it.isAlcoholic }
            else -> all
        }
        _isEmpty.value = _results.value.isNullOrEmpty() && isSearchActive
    }

    fun toggleFavorite(preview: CocktailPreview) {
        viewModelScope.launch {
            toggleFavoriteUseCase(preview, favoriteIds.value ?: emptySet())
        }
    }

    fun clearRecentSearches() {
        recentSearchRepository.clear()
        _recentSearches.value = emptyList()
    }

    private fun saveRecentSearch(query: String) {
        recentSearchRepository.save(query)
        _recentSearches.value = recentSearchRepository.getAll()
    }

    private fun loadRecentSearches() {
        _recentSearches.value = recentSearchRepository.getAll()
    }
}
