package com.example.ratonean2_app.places.di

import com.example.ratonean2_app.places.data.provider.PlacesProviderImpl
import com.example.ratonean2_app.places.domain.provider.PlacesProvider
import com.example.ratonean2_app.places.domain.usecase.GetPlacesUseCase
import org.koin.dsl.module

val placesModule = module {
    single<PlacesProvider> { PlacesProviderImpl(get(), get()) }
    factory { GetPlacesUseCase(get()) }
}