package com.shakerlab.app.domain.usecase.cocktail

import com.shakerlab.app.domain.repository.CocktailRepository

class GetCategoriesUseCase(private val repository: CocktailRepository) {
    suspend operator fun invoke(): List<String> = repository.getCategories()
}
