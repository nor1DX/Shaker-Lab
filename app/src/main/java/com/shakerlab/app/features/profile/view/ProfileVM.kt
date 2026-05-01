package com.shakerlab.app.features.profile.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.shakerlab.app.domain.repository.BarRepository
import com.shakerlab.app.domain.repository.FavoritesRepository
import kotlinx.coroutines.launch

class ProfileVM(
    private val favoritesRepository: FavoritesRepository,
    private val barRepository: BarRepository
) : ViewModel() {

    val favoritesCount: LiveData<Int> = favoritesRepository.getAll().map { it.size }
    val barCount: LiveData<Int> = barRepository.getIngredients().map { it.size }

    fun clearFavorites() {
        viewModelScope.launch { favoritesRepository.clearAll() }
    }

    fun clearBar() {
        viewModelScope.launch { barRepository.clearAll() }
    }
}