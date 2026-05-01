package com.shakerlab.app

import android.app.Application
import com.shakerlab.app.di.appModule
import com.shakerlab.app.di.dataModule
import com.shakerlab.app.di.domainModule
import com.shakerlab.app.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(networkModule, dataModule, domainModule, appModule)
        }
    }
}