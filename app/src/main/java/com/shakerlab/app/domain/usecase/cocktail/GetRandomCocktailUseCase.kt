package com.shakerlab.app.domain.usecase.cocktail

import com.shakerlab.app.domain.model.Cocktail
import com.shakerlab.app.domain.repository.CocktailRepository

class GetRandomCocktailUseCase(private val repository: CocktailRepository) {
    suspend operator fun invoke(): Cocktail = repository.getRandom()
}
