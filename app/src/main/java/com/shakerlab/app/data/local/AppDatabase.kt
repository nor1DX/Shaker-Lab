package com.shakerlab.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteEntity::class, BarIngredientEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun barIngredientDao(): BarIngredientDao
}