package com.shakerlab.app.domain.model

data class CocktailPreview(
    val id: String,
    val name: String,
    val thumbnail: String,
    val category: String = "",
    val isAlcoholic: Boolean = false
)
