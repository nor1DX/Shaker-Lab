package com.shakerlab.app.features.detail.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakerlab.app.domain.model.Cocktail
import com.shakerlab.app.domain.repository.CocktailRepository
import com.shakerlab.app.domain.repository.FavoritesRepository
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: CocktailRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _cocktail = MutableLiveData<Cocktail?>()
    val cocktail: LiveData<Cocktail?> = _cocktail

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _isFavorite = MediatorLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite
    private var favoriteSource: LiveData<Boolean>? = null

    fun loadCocktail(id: String) {
        favoriteSource?.let { _isFavorite.removeSource(it) }
        val src = favoritesRepository.isFavorite(id)
        favoriteSource = src
        _isFavorite.addSource(src) { _isFavorite.value = it }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _cocktail.value = repository.getById(id)
            } catch (e: Exception) {
                _error.value = "Failed to load cocktail"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getNextRandom() {
        if (_isLoading.value == true) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val next = repository.getRandom()
                loadCocktail(next.id)
            } catch (e: Exception) {
                _error.value = "Failed to load random cocktail"
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite() {
        val cocktail = _cocktail.value ?: return
        val currentlyFavorite = _isFavorite.value ?: false
        viewModelScope.launch {
            if (currentlyFavorite) favoritesRepository.remove(cocktail.id)
            else favoritesRepository.add(cocktail)
        }
    }
}
