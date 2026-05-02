package com.shakerlab.app.features.search.view

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.CocktailRepository
import com.shakerlab.app.domain.repository.FavoritesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val PREFS_NAME = "search_prefs"
private const val KEY_RECENT = "recent_searches"
private const val MAX_RECENT = 8

class SearchVM(
    app: Application,
    private val repository: CocktailRepository,
    private val favoritesRepository: FavoritesRepository
) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _allResults = MutableLiveData<List<CocktailPreview>>(emptyList())
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

    val favoriteIds: LiveData<Set<String>> = favoritesRepository.getAll().map { list ->
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
                val cocktails = repository.searchByName(trimmed)
                _allResults.value = cocktails
                applyFilter()
                _isEmpty.value = _results.value.isNullOrEmpty()
                saveRecentSearch(trimmed)
            } catch (e: Exception) {
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
        val all = _allResults.value ?: emptyList()
        _results.value = when (alcoholicFilter) {
            "Alcoholic" -> all.filter { it.isAlcoholic }
            "Non-alcoholic" -> all.filter { !it.isAlcoholic }
            else -> all
        }
        _isEmpty.value = _results.value.isNullOrEmpty() && isSearchActive
    }

    fun loadMore() {
        // No random results appended after name search — API returns all matches at once
        if (isSearchActive) return
    }

    fun clearResults() {
        searchJob?.cancel()
        isSearchActive = false
        _allResults.value = emptyList()
        _results.value = emptyList()
        _isEmpty.value = false
        _error.value = null
        _isLoading.value = false
    }

    fun toggleFavorite(preview: CocktailPreview) {
        viewModelScope.launch {
            if (preview.id in (favoriteIds.value ?: emptySet())) {
                favoritesRepository.remove(preview.id)
            } else {
                favoritesRepository.addPreview(preview)
            }
        }
    }

    fun clearRecentSearches() {
        prefs.edit().remove(KEY_RECENT).apply()
        _recentSearches.value = emptyList()
    }

    private fun saveRecentSearch(query: String) {
        val current = loadRecentFromPrefs().toMutableList()
        current.remove(query)
        current.add(0, query)
        if (current.size > MAX_RECENT) current.removeAt(current.lastIndex)
        prefs.edit().putString(KEY_RECENT, current.joinToString("|||")).apply()
        _recentSearches.value = current
    }

    private fun loadRecentSearches() {
        _recentSearches.value = loadRecentFromPrefs()
    }

    private fun loadRecentFromPrefs(): List<String> {
        val raw = prefs.getString(KEY_RECENT, "") ?: ""
        return if (raw.isEmpty()) emptyList() else raw.split("|||")
    }
}
