package com.shakerlab.app.data.repository

import com.shakerlab.app.data.remote.model.CocktailDetailDto
import com.shakerlab.app.data.remote.model.CocktailPreviewDto
import com.shakerlab.app.domain.model.Cocktail
import com.shakerlab.app.domain.model.CocktailPreview
import com.shakerlab.app.domain.model.Ingredient

fun CocktailDetailDto.toDomain(): Cocktail {
    val ingredients = listOf(
        ingredient1 to measure1, ingredient2 to measure2, ingredient3 to measure3,
        ingredient4 to measure4, ingredient5 to measure5, ingredient6 to measure6,
        ingredient7 to measure7, ingredient8 to measure8, ingredient9 to measure9,
        ingredient10 to measure10, ingredient11 to measure11, ingredient12 to measure12,
        ingredient13 to measure13, ingredient14 to measure14, ingredient15 to measure15
    )
        .filter { !it.first.isNullOrBlank() }
        .map { Ingredient(name = it.first!!, measure = it.second.orEmpty().trim()) }

    return Cocktail(
        id = id,
        name = name,
        category = category.orEmpty(),
        isAlcoholic = alcoholic == "Alcoholic",
        glass = glass.orEmpty(),
        instructions = instructions.orEmpty(),
        thumbnail = thumbnail.orEmpty(),
        ingredients = ingredients
    )
}

fun CocktailDetailDto.toPreview() = CocktailPreview(
    id = id,
    name = name,
    thumbnail = thumbnail.orEmpty(),
    category = category.orEmpty()
)

fun CocktailPreviewDto.toDomain() = CocktailPreview(
    id = id,
    name = name,
    thumbnail = thumbnail.orEmpty()
)