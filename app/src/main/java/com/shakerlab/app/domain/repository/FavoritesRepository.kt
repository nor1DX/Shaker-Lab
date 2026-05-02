package com.shakerlab.app.domain.repository

import androidx.lifecycle.LiveData
import com.shakerlab.app.domain.model.Cocktail
import com.shakerlab.app.domain.model.CocktailPreview

interface FavoritesRepository {
    fun getAll(): LiveData<List<CocktailPreview>>
    fun isFavorite(id: String): LiveData<Boolean>
    suspend fun add(cocktail: Cocktail)
    suspend fun addPreview(preview: CocktailPreview)
    suspend fun remove(id: String)
    suspend fun clearAll()
    suspend fun syncFromCloud()
}
