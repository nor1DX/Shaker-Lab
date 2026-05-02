package com.shakerlab.app.domain.model

data class Cocktail(
    val id: String,
    val name: String,
    val category: String,
    val isAlcoholic: Boolean,
    val glass: String,
    val instructions: String,
    val thumbnail: String,
    val ingredients: List<Ingredient>
)