package com.shakerlab.app.domain.usecase.cocktail

import com.shakerlab.app.domain.model.Cocktail
import com.shakerlab.app.domain.repository.CocktailRepository

class GetCocktailByIdUseCase(private val repository: CocktailRepository) {
    suspend operator fun invoke(id: String): Cocktail = repository.getById(id)
}
