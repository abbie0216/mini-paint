package com.abbie.minipaint

import android.app.Application
import com.abbie.minipaint.di.appModule
import com.abbie.minipaint.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        val module = listOf(
            appModule,
            viewModelModule
        )

        startKoin {
            androidContext(this@App)
            modules(module)
        }
    }

}