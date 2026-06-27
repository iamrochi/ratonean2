package com.example.ratonean2_app.map.di

import com.example.ratonean2_app.map.data.provider.FusedLocationProviderImpl
import com.example.ratonean2_app.map.domain.provider.LocationProvider
import com.example.ratonean2_app.map.domain.usercase.GetUserLocationUseCase
import com.example.ratonean2_app.map.presentation.viewmodel.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val locationModule = module {
    single<LocationProvider> { FusedLocationProviderImpl(get()) }
    factory { GetUserLocationUseCase(get()) }
    viewModel {
        MapViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
