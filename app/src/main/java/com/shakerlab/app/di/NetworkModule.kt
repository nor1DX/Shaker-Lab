package com.shakerlab.app.di

import com.shakerlab.app.data.remote.CocktailService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/"

private fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .build()

private fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private fun provideCocktailService(retrofit: Retrofit): CocktailService =
    retrofit.create(CocktailService::class.java)

val networkModule = module {
    single { provideOkHttpClient() }
    single { provideRetrofit(get()) }
    single { provideCocktailService(get()) }
}
