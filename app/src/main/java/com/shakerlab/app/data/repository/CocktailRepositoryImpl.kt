package com.shakerlab.app.data.repository

import com.shakerlab.app.data.remote.CocktailService
import com.shakerlab.app.domain.model.Cocktail
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.repository.CocktailRepository

class CocktailRepositoryImpl(private val service: CocktailService) : CocktailRepository {

    override suspend fun searchByName(name: String): List<CocktailPreview> =
        service.searchByName(name).drinks?.map { it.toPreview() } ?: emptyList()

    override suspend fun getById(id: String): Cocktail =
        service.lookupById(id).drinks?.first()?.toDomain()
            ?: error("Cocktail $id not found")

    override suspend fun getRandom(): Cocktail =
        service.getRandom().drinks?.first()?.toDomain()
            ?: error("Failed to load random cocktail")

    override suspend fun filterByCategory(category: String): List<CocktailPreview> =
        service.filterByCategory(category).drinks?.map { it.toPreview().copy(category = category) } ?: emptyList()

    override suspend fun filterByAlcoholic(alcoholic: String): List<CocktailPreview> =
        service.filterByAlcoholic(alcoholic).drinks?.map { it.toPreview() } ?: emptyList()

    override suspend fun filterByIngredient(ingredient: String): List<CocktailPreview> =
        service.filterByIngredient(ingredient).drinks?.map { it.toPreview() } ?: emptyList()

    override suspend fun getCategories(): List<String> =
        service.listCategories().drinks?.map { it.name } ?: emptyList()

    override suspend fun getIngredients(): List<String> =
        service.listIngredients().drinks?.map { it.name } ?: emptyList()
}