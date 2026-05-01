package com.shakerlab.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val cocktailId: String,
    val name: String,
    val thumbnail: String,
    val category: String
)