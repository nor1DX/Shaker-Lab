package com.shakerlab.app.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY rowid DESC")
    fun getAll(): LiveData<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE cocktailId = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) > 0 FROM favorites WHERE cocktailId = :id")
    fun exists(id: String): LiveData<Boolean>

    @Query("DELETE FROM favorites")
    suspend fun clearAll()
}