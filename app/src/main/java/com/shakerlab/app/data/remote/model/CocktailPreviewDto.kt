package com.shakerlab.app.data.remote.model

import com.google.gson.annotations.SerializedName

data class CocktailPreviewResponse(
    @SerializedName("drinks") val drinks: List<CocktailPreviewDto>?
)

data class CocktailPreviewDto(
    @SerializedName("idDrink") val id: String,
    @SerializedName("strDrink") val name: String,
    @SerializedName("strDrinkThumb") val thumbnail: String?
)