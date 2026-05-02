package com.shakerlab.app.domain.usecase.cocktail

import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.CocktailRepository

class FilterByCategoryUseCase(private val repository: CocktailRepository) {
    suspend operator fun invoke(category: String): List<CocktailPreview> =
        repository.filterByCategory(category)
}
