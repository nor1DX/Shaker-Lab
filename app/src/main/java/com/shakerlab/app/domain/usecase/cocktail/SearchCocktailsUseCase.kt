package com.shakerlab.app.domain.usecase.cocktail

import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.CocktailRepository

class SearchCocktailsUseCase(private val repository: CocktailRepository) {
    suspend operator fun invoke(query: String): List<CocktailPreview> =
        repository.searchByName(query)
}
