package com.shakerlab.app.di

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shakerlab.app.data.local.AppDatabase
import com.shakerlab.app.data.repository.BarRepositoryImpl
import com.shakerlab.app.data.repository.CocktailRepositoryImpl
import com.shakerlab.app.data.repository.FavoritesRepositoryImpl
import com.shakerlab.app.domain.repository.BarRepository
import com.shakerlab.app.domain.repository.CocktailRepository
import com.shakerlab.app.domain.repository.FavoritesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "shakerlab.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<AppDatabase>().favoriteDao() }
    single { get<AppDatabase>().barIngredientDao() }
    single<CocktailRepository> { CocktailRepositoryImpl(get()) }
    single<FavoritesRepository> { FavoritesRepositoryImpl(get(), get(), get()) }
    single<BarRepository> { BarRepositoryImpl(get(), get(), get()) }
}
