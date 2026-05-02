package com.shakerlab.app.features.favorites.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.FavoritesRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {

    val favorites: LiveData<List<CocktailPreview>> = repository.getAll()

    val favoriteIds: LiveData<Set<String>> = repository.getAll().map { list ->
        list.map { it.id }.toSet()
    }

    fun toggleFavorite(preview: CocktailPreview) {
        viewModelScope.launch { repository.remove(preview.id) }
    }
}
