package com.shakerlab.app.domain.usecase.favorites

import com.shakerlab.app.domain.model.Cocktail
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.FavoritesRepository

class ToggleFavoriteUseCase(private val repository: FavoritesRepository) {
    suspend operator fun invoke(preview: CocktailPreview, favoriteIds: Set<String>) {
        if (preview.id in favoriteIds) repository.remove(preview.id)
        else repository.addPreview(preview)
    }

    suspend operator fun invoke(cocktail: Cocktail, isFavorite: Boolean) {
        if (isFavorite) repository.remove(cocktail.id)
        else repository.add(cocktail)
    }
}
