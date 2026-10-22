package com.example.ratonean2_app.core.di

import com.example.ratonean2_app.core.network.ConnectivityObserver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { ConnectivityObserver(androidContext()) }
}
