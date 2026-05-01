package com.shakerlab.app.features.favorites.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.FavoritesRepository

class FavoritesVM(private val repository: FavoritesRepository) : ViewModel() {
    val favorites: LiveData<List<CocktailPreview>> = repository.getAll()
}