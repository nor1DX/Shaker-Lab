package com.shakerlab.app.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BarIngredientDao {
    @Query("SELECT name FROM bar_ingredients ORDER BY name ASC")
    fun getAll(): LiveData<List<String>>

    @Query("SELECT name FROM bar_ingredients")
    suspend fun getAllOnce(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: BarIngredientEntity)

    @Query("DELETE FROM bar_ingredients WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("DELETE FROM bar_ingredients")
    suspend fun clearAll()
}