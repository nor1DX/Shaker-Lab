package com.shakerlab.app.features.catalog.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.CocktailRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class CatalogVM(private val repository: CocktailRepository) : ViewModel() {

    private val _cocktails = MutableLiveData<List<CocktailPreview>>()
    val cocktails: LiveData<List<CocktailPreview>> = _cocktails

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _randomId = MutableLiveData<String?>()
    val randomId: LiveData<String?> = _randomId

    private var currentCategory = ""
    private val seen = mutableSetOf<String>()

    init {
        loadCategories()
    }

    fun loadByCategory(category: String) {
        if (category == currentCategory) return
        currentCategory = category
        seen.clear()
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val cocktails = repository.filterByCategory(category)
                cocktails.forEach { seen.add(it.id) }
                _cocktails.value = cocktails
            } catch (e: Exception) {
                _error.value = "Failed to load cocktails"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getRandom() {
        if (_isLoading.value == true) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cocktail = repository.getRandom()
                _randomId.value = cocktail.id
                _randomId.value = null
            } catch (_: Exception) { }
            finally { _isLoading.value = false }
        }
    }

    fun loadMore() {
        if (_isLoading.value == true) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newItems = coroutineScope {
                    (1..12).map { async { repository.getRandom() } }.awaitAll()
                }
                    .filter { seen.add(it.id) }
                    .map { CocktailPreview(it.id, it.name, it.thumbnail, it.category) }
                _cocktails.value = (_cocktails.value ?: emptyList()) + newItems
            } catch (_: Exception) { }
            finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val cats = repository.getCategories()
                _categories.value = cats
                if (cats.isNotEmpty()) loadByCategory(cats.first())
            } catch (e: Exception) {
                _error.value = "Failed to load categories"
            }
        }
    }
}