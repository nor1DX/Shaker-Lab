package com.shakerlab.app.data.repository

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shakerlab.app.data.local.BarIngredientDao
import com.shakerlab.app.data.local.BarIngredientEntity
import com.shakerlab.app.domain.repository.BarRepository
import kotlinx.coroutines.tasks.await

class BarRepositoryImpl(
    private val dao: BarIngredientDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : BarRepository {

    private fun barCol() = auth.currentUser?.uid?.let {
        firestore.collection("users").document(it).collection("bar")
    }

    override fun getIngredients(): LiveData<List<String>> = dao.getAll()

    override suspend fun add(name: String) {
        dao.insert(BarIngredientEntity(name))
        barCol()?.document(name)?.set(mapOf("name" to name))
    }

    override suspend fun remove(name: String) {
        dao.deleteByName(name)
        barCol()?.document(name)?.delete()
    }

    override suspend fun clearAll() {
        dao.clearAll()
        val col = barCol() ?: return
        val docs = col.get().await()
        if (docs.isEmpty) return
        val batch = firestore.batch()
        docs.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    override suspend fun syncFromCloud() {
        val col = barCol() ?: return
        val docs = col.get().await()
        dao.clearAll()
        docs.forEach { doc ->
            dao.insert(BarIngredientEntity(doc.getString("name") ?: doc.id))
        }
    }
}
