package com.shakerlab.app.domain.repository

import androidx.lifecycle.LiveData

interface BarRepository {
    fun getIngredients(): LiveData<List<String>>
    suspend fun add(name: String)
    suspend fun remove(name: String)
    suspend fun clearAll()
    suspend fun syncFromCloud()
}