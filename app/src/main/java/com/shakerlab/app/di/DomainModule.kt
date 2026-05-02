package com.shakerlab.app.di

import com.shakerlab.app.domain.usecase.bar.AddBarIngredientUseCase
import com.shakerlab.app.domain.usecase.bar.ClearBarUseCase
import com.shakerlab.app.domain.usecase.bar.GetBarIngredientsUseCase
import com.shakerlab.app.domain.usecase.bar.RemoveBarIngredientUseCase
import com.shakerlab.app.domain.usecase.bar.SyncBarUseCase
import com.shakerlab.app.domain.usecase.cocktail.FilterByCategoryUseCase
import com.shakerlab.app.domain.usecase.cocktail.GetAllIngredientsUseCase
import com.shakerlab.app.domain.usecase.cocktail.GetCategoriesUseCase
import com.shakerlab.app.domain.usecase.cocktail.GetCocktailByIdUseCase
import com.shakerlab.app.domain.usecase.cocktail.GetRandomCocktailUseCase
import com.shakerlab.app.domain.usecase.cocktail.SearchCocktailsUseCase
import com.shakerlab.app.domain.usecase.favorites.ClearFavoritesUseCase
import com.shakerlab.app.domain.usecase.favorites.GetFavoritesUseCase
import com.shakerlab.app.domain.usecase.favorites.IsFavoriteUseCase
import com.shakerlab.app.domain.usecase.favorites.SyncFavoritesUseCase
import com.shakerlab.app.domain.usecase.favorites.ToggleFavoriteUseCase
import org.koin.dsl.module

val domainModule = module {
    // Cocktail
    factory { GetCocktailByIdUseCase(get()) }
    factory { GetRandomCocktailUseCase(get()) }
    factory { GetCategoriesUseCase(get()) }
    factory { FilterByCategoryUseCase(get()) }
    factory { SearchCocktailsUseCase(get()) }
    factory { GetAllIngredientsUseCase(get()) }

    // Favorites
    factory { GetFavoritesUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }
    factory { IsFavoriteUseCase(get()) }
    factory { SyncFavoritesUseCase(get()) }
    factory { ClearFavoritesUseCase(get()) }

    // Bar
    factory { GetBarIngredientsUseCase(get()) }
    factory { AddBarIngredientUseCase(get()) }
    factory { RemoveBarIngredientUseCase(get()) }
    factory { SyncBarUseCase(get()) }
    factory { ClearBarUseCase(get()) }
}
