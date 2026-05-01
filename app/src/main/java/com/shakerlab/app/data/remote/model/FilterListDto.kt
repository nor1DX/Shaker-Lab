package com.shakerlab.app.data.remote.model

import com.google.gson.annotations.SerializedName

data class CategoryListResponse(
    @SerializedName("drinks") val drinks: List<CategoryDto>?
)

data class CategoryDto(
    @SerializedName("strCategory") val name: String
)

data class IngredientListResponse(
    @SerializedName("drinks") val drinks: List<IngredientDto>?
)

data class IngredientDto(
    @SerializedName("strIngredient1") val name: String
)