package com.shakerlab.app.domain.usecase.favorites

import androidx.lifecycle.LiveData
import com.shakerlab.app.domain.repository.FavoritesRepository

class IsFavoriteUseCase(private val repository: FavoritesRepository) {
    operator fun invoke(id: String): LiveData<Boolean> = repository.isFavorite(id)
}
