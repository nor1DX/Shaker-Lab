package com.shakerlab.app.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.shakerlab.app.data.local.FavoriteDao
import com.shakerlab.app.data.local.FavoriteEntity
import com.shakerlab.app.domain.model.Cocktail
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.FavoritesRepository

class FavoritesRepositoryImpl(private val dao: FavoriteDao) : FavoritesRepository {

    override fun getAll(): LiveData<List<CocktailPreview>> =
        dao.getAll().map { list ->
            list.map { CocktailPreview(it.cocktailId, it.name, it.thumbnail, it.category) }
        }

    override fun isFavorite(id: String): LiveData<Boolean> = dao.exists(id)

    override suspend fun add(cocktail: Cocktail) {
        dao.insert(FavoriteEntity(cocktail.id, cocktail.name, cocktail.thumbnail, cocktail.category))
    }

    override suspend fun remove(id: String) {
        dao.deleteById(id)
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}