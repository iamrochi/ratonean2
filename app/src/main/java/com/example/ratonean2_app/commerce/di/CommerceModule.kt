package com.example.ratonean2_app.commerce.di

import com.example.ratonean2_app.commerce.data.local.CommerceLocalDataSource
import com.example.ratonean2_app.commerce.data.provider.CommerceProviderImpl
import com.example.ratonean2_app.commerce.data.repository.CommerceRepositoryImpl
import com.example.ratonean2_app.commerce.domain.provider.CommerceProvider
import com.example.ratonean2_app.commerce.domain.repository.CommerceRepository
import com.example.ratonean2_app.commerce.domain.usecase.CreateCommerceUseCase
import com.example.ratonean2_app.commerce.domain.usecase.DeleteCommerceUseCase
import com.example.ratonean2_app.commerce.domain.usecase.GetCommerceByIdUseCase
import com.example.ratonean2_app.commerce.domain.usecase.GetCommercesUseCase
import com.example.ratonean2_app.commerce.domain.usecase.UpdateCommerceUseCase
import com.example.ratonean2_app.core.data.database.CatalogDatabase
import org.koin.dsl.module

val commerceModule = module {
    single<CommerceProvider> { CommerceProviderImpl(get()) }
    factory { GetCommercesUseCase(get()) }
    factory { GetCommerceByIdUseCase(get()) }
    factory { CreateCommerceUseCase(get()) }
    factory { UpdateCommerceUseCase(get()) }
    factory { DeleteCommerceUseCase(get()) }

    single { get<CatalogDatabase>().commerceDao() }
    single { CommerceLocalDataSource(get()) }

    single<CommerceRepository> { CommerceRepositoryImpl(get(), get()) }
}