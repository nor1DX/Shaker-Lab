package com.shakerlab.app.data.remote

import com.shakerlab.app.data.remote.model.CategoryListResponse
import com.shakerlab.app.data.remote.model.CocktailDetailResponse
import com.shakerlab.app.data.remote.model.CocktailPreviewResponse
import com.shakerlab.app.data.remote.model.IngredientListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CocktailService {

    @GET("search.php")
    suspend fun searchByName(@Query("s") name: String): CocktailDetailResponse

    @GET("lookup.php")
    suspend fun lookupById(@Query("i") id: String): CocktailDetailResponse

    @GET("random.php")
    suspend fun getRandom(): CocktailDetailResponse

    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): CocktailPreviewResponse

    @GET("filter.php")
    suspend fun filterByAlcoholic(@Query("a") alcoholic: String): CocktailPreviewResponse

    @GET("filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): CocktailPreviewResponse

    @GET("list.php")
    suspend fun listCategories(@Query("c") list: String = "list"): CategoryListResponse

    @GET("list.php")
    suspend fun listIngredients(@Query("i") list: String = "list"): IngredientListResponse
}