package com.shakerlab.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bar_ingredients")
data class BarIngredientEntity(
    @PrimaryKey val name: String
)