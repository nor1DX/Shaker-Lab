package com.shakerlab.app.domain.usecase.cocktail

import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.CocktailRepository

class FilterByIngredientUseCase(private val repository: CocktailRepository) {
    suspend operator fun invoke(ingredient: String): List<CocktailPreview> =
        repository.filterByIngredient(ingredient)
}
