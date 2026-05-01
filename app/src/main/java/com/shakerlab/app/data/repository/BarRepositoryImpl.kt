package com.shakerlab.app.data.repository

import androidx.lifecycle.LiveData
import com.shakerlab.app.data.local.BarIngredientDao
import com.shakerlab.app.data.local.BarIngredientEntity
import com.shakerlab.app.domain.repository.BarRepository

class BarRepositoryImpl(private val dao: BarIngredientDao) : BarRepository {
    override fun getIngredients(): LiveData<List<String>> = dao.getAll()
    override suspend fun add(name: String) = dao.insert(BarIngredientEntity(name))
    override suspend fun remove(name: String) = dao.deleteByName(name)
    override suspend fun clearAll() = dao.clearAll()
}