package com.shakerlab.app.domain.usecase.bar

import com.shakerlab.app.domain.repository.BarRepository

class AddBarIngredientUseCase(private val repository: BarRepository) {
    suspend operator fun invoke(name: String) = repository.add(name)
}
