package com.shakerlab.app.domain.usecase.favorites

import androidx.lifecycle.LiveData
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.FavoritesRepository

class GetFavoritesUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(): LiveData<List<CocktailPreview>> = repository.getAll()
}
