package com.shakerlab.app.di

import com.shakerlab.app.features.catalog.view.CatalogViewModel
import com.shakerlab.app.features.detail.view.DetailViewModel
import com.shakerlab.app.features.favorites.view.FavoritesViewModel
import com.shakerlab.app.features.mybar.view.MyBarViewModel
import com.shakerlab.app.features.profile.view.ProfileViewModel
import com.shakerlab.app.features.search.view.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::CatalogViewModel)
    viewModelOf(::DetailViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::MyBarViewModel)
    viewModelOf(::ProfileViewModel)
}