package com.shakerlab.app.domain.usecase.bar

import com.shakerlab.app.domain.repository.BarRepository

class SyncBarUseCase(private val repository: BarRepository) {
    suspend operator fun invoke() = repository.syncFromCloud()
}
