package com.shakerlab.app.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shakerlab.app.data.local.FavoriteDao
import com.shakerlab.app.data.local.FavoriteEntity
import com.shakerlab.app.domain.model.Cocktail
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.FavoritesRepository
import kotlinx.coroutines.tasks.await

class FavoritesRepositoryImpl(
    private val dao: FavoriteDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : FavoritesRepository {

    private fun favCol() = auth.currentUser?.uid?.let {
        firestore.collection("users").document(it).collection("favorites")
    }

    override fun getAll(): LiveData<List<CocktailPreview>> =
        dao.getAll().map { list ->
            list.map { CocktailPreview(it.cocktailId, it.name, it.thumbnail, it.category) }
        }

    override fun isFavorite(id: String): LiveData<Boolean> = dao.exists(id)

    override suspend fun add(cocktail: Cocktail) {
        dao.insert(FavoriteEntity(cocktail.id, cocktail.name, cocktail.thumbnail, cocktail.category))
        favCol()?.document(cocktail.id)?.set(
            mapOf("name" to cocktail.name, "thumbnail" to cocktail.thumbnail, "category" to cocktail.category)
        )
    }

    override suspend fun addPreview(preview: CocktailPreview) {
        dao.insert(FavoriteEntity(preview.id, preview.name, preview.thumbnail, preview.category))
        favCol()?.document(preview.id)?.set(
            mapOf("name" to preview.name, "thumbnail" to preview.thumbnail, "category" to preview.category)
        )
    }

    override suspend fun remove(id: String) {
        dao.deleteById(id)
        favCol()?.document(id)?.delete()
    }

    override suspend fun clearAll() {
        dao.clearAll()
        val col = favCol() ?: return
        val docs = col.get().await()
        if (docs.isEmpty) return
        val batch = firestore.batch()
        docs.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    override suspend fun syncFromCloud() {
        val col = favCol() ?: return
        val docs = col.get().await()
        dao.clearAll()
        docs.forEach { doc ->
            dao.insert(
                FavoriteEntity(
                    cocktailId = doc.id,
                    name = doc.getString("name") ?: "",
                    thumbnail = doc.getString("thumbnail") ?: "",
                    category = doc.getString("category") ?: ""
                )
            )
        }
    }
}
