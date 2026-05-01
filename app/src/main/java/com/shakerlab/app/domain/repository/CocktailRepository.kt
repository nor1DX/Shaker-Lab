package com.shakerlab.app.domain.repository

import com.shakerlab.app.domain.model.Cocktail
import com.shakerlab.app.domain.model.CocktailPreview

interface CocktailRepository {
    suspend fun searchByName(name: String): List<CocktailPreview>
    suspend fun getById(id: String): Cocktail
    suspend fun getRandom(): Cocktail
    suspend fun filterByCategory(category: String): List<CocktailPreview>
    suspend fun filterByAlcoholic(alcoholic: String): List<CocktailPreview>
    suspend fun filterByIngredient(ingredient: String): List<CocktailPreview>
    suspend fun getCategories(): List<String>
    suspend fun getIngredients(): List<String>
}