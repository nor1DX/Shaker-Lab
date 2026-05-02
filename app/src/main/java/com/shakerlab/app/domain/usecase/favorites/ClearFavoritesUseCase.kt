package com.shakerlab.app.domain.usecase.favorites

import com.shakerlab.app.domain.repository.FavoritesRepository

class ClearFavoritesUseCase(private val repository: FavoritesRepository) {
    suspend operator fun invoke() = repository.clearAll()
}
