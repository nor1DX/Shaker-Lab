package com.shakerlab.app.di

import com.shakerlab.app.features.catalog.view.CatalogVM
import com.shakerlab.app.features.detail.view.DetailVM
import com.shakerlab.app.features.favorites.view.FavoritesVM
import com.shakerlab.app.features.mybar.view.MyBarVM
import com.shakerlab.app.features.profile.view.ProfileVM
import com.shakerlab.app.features.search.view.SearchVM
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::CatalogVM)
    viewModelOf(::DetailVM)
    viewModelOf(::SearchVM)
    viewModelOf(::FavoritesVM)
    viewModelOf(::MyBarVM)
    viewModelOf(::ProfileVM)
}