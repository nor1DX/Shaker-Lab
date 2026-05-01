package com.shakerlab.app.features.mybar.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.BarRepository
import com.shakerlab.app.domain.repository.CocktailRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MyBarVM(
    private val barRepository: BarRepository,
    private val cocktailRepository: CocktailRepository
) : ViewModel() {

    val barIngredients: LiveData<List<String>> = barRepository.getIngredients()

    private val _cocktails = MutableLiveData<List<CocktailPreview>>(emptyList())
    val cocktails: LiveData<List<CocktailPreview>> = _cocktails

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _allIngredients = MutableLiveData<List<String>>(emptyList())
    val allIngredients: LiveData<List<String>> = _allIngredients

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
            try {
                _allIngredients.value = cocktailRepository.getIngredients()
            } catch (_: Exception) { }
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
                _cocktails.value = fetchRandomPage()
            } catch (_: Exception) {
                _cocktails.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoading.value == true) return
        if ((barIngredients.value ?: emptyList()).isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newItems = fetchRandomPage()
                _cocktails.value = (_cocktails.value ?: emptyList()) + newItems
            } catch (_: Exception) { }
            finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchRandomPage(): List<CocktailPreview> {
        val randoms = coroutineScope {
            (1..12).map { async { cocktailRepository.getRandom() } }.awaitAll()
        }
        return randoms
            .filter { seen.add(it.id) }
            .map { CocktailPreview(it.id, it.name, it.thumbnail, it.category) }
    }

    fun refresh() {
        fetchMatchingCocktails(barIngredients.value ?: emptyList())
    }

    fun addIngredient(name: String) {
        viewModelScope.launch { barRepository.add(name.trim()) }
    }

    fun removeIngredient(name: String) {
        viewModelScope.launch { barRepository.remove(name) }
    }

    override fun onCleared() {
        super.onCleared()
        barIngredients.removeObserver(ingredientsObserver)
    }
}