package com.shakerlab.app.domain.usecase.bar

import com.shakerlab.app.domain.repository.BarRepository

class ClearBarUseCase(private val repository: BarRepository) {
    suspend operator fun invoke() = repository.clearAll()
}
