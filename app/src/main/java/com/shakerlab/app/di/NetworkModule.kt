package com.shakerlab.app.di

import android.content.Context
import com.shakerlab.app.data.remote.CocktailService
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/"
private const val CACHE_SIZE = 10L * 1024 * 1024

private fun provideOkHttpClient(context: Context): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .cache(Cache(File(context.cacheDir, "http_cache"), CACHE_SIZE))
    .addNetworkInterceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)
        val path = request.url.encodedPath
        if (path.contains("random.php")) {
            response // never cache random
        } else {
            val maxAge = if (path.contains("list.php")) 43200 else 3600
            response.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", "public, max-age=$maxAge")
                .build()
        }
    }
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
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
    single { provideOkHttpClient(androidContext()) }
    single { provideRetrofit(get()) }
    single { provideCocktailService(get()) }
}
