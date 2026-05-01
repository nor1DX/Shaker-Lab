package com.shakerlab.app.features.search.view

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.CocktailRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private const val PREFS_NAME = "search_prefs"
private const val KEY_RECENT = "recent_searches"
private const val MAX_RECENT = 8

class SearchVM(
    app: Application,
    private val repository: CocktailRepository
) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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

    private var searchJob: Job? = null
    private val seen = mutableSetOf<String>()

    init {
        loadRecentSearches()
    }

    fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        searchJob?.cancel()
        seen.clear()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isEmpty.value = false
            try {
                val cocktails = repository.searchByName(trimmed)
                cocktails.forEach { seen.add(it.id) }
                _results.value = cocktails
                _isEmpty.value = cocktails.isEmpty()
                saveRecentSearch(trimmed)
            } catch (e: Exception) {
                _error.value = "Search error"
                _results.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoading.value == true) return
        if ((_results.value ?: emptyList()).isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newItems = coroutineScope {
                    (1..12).map { async { repository.getRandom() } }.awaitAll()
                }
                    .filter { seen.add(it.id) }
                    .map { CocktailPreview(it.id, it.name, it.thumbnail, it.category) }
                _results.value = (_results.value ?: emptyList()) + newItems
            } catch (_: Exception) { }
            finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        searchJob?.cancel()
        seen.clear()
        _results.value = emptyList()
        _isEmpty.value = false
        _error.value = null
        _isLoading.value = false
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