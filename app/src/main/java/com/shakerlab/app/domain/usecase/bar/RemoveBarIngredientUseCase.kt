package com.shakerlab.app.domain.usecase.bar

import com.shakerlab.app.domain.repository.BarRepository

class RemoveBarIngredientUseCase(private val repository: BarRepository) {
    suspend operator fun invoke(name: String) = repository.remove(name)
}
