package com.shakerlab.app.domain.usecase.bar

import androidx.lifecycle.LiveData
import com.shakerlab.app.domain.repository.BarRepository

class GetBarIngredientsUseCase(private val repository: BarRepository) {
    operator fun invoke(): LiveData<List<String>> = repository.getIngredients()
}
