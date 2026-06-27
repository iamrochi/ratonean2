package com.example.ratonean2_app.product.di

import com.example.ratonean2_app.core.data.database.AppDatabase
import com.example.ratonean2_app.product.data.local.ProductLocalDataSource
import com.example.ratonean2_app.product.data.provider.ProductProviderImpl
import com.example.ratonean2_app.product.data.repository.ProductRepositoryImpl
import com.example.ratonean2_app.product.domain.provider.ProductProvider
import com.example.ratonean2_app.product.domain.repository.ProductRepository
import com.example.ratonean2_app.product.domain.usecase.CreateNewProductUseCase
import com.example.ratonean2_app.product.domain.usecase.DeleteProductUseCase
import com.example.ratonean2_app.product.domain.usecase.GetAllProductsUseCase
import com.example.ratonean2_app.product.domain.usecase.GetPopularProductsUseCase
import com.example.ratonean2_app.product.domain.usecase.GetProductByIdUseCase
import com.example.ratonean2_app.product.domain.usecase.GetProductsByBranchUseCase
import com.example.ratonean2_app.product.domain.usecase.GetProductsBySearchInBranches
import com.example.ratonean2_app.product.domain.usecase.GetProductsNearbyUseCase
import com.example.ratonean2_app.product.domain.usecase.GetProductsWithPromosUseCase
import com.example.ratonean2_app.product.domain.usecase.UpdateProductUseCase
import com.example.ratonean2_app.product.presentation.viewmodel.ProductViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val productModule = module {
    single<ProductProvider> { ProductProviderImpl(get()) }
    factory { CreateNewProductUseCase(get()) }
    factory { GetAllProductsUseCase(get()) }
    factory { GetProductByIdUseCase(get()) }
    factory { UpdateProductUseCase(get()) }
    factory { DeleteProductUseCase(get()) }
    viewModel { ProductViewModel(get()) }
    factory { GetProductsByBranchUseCase(get()) }
    factory { GetProductsWithPromosUseCase(get()) }
    factory { GetProductsBySearchInBranches(get()) }
    factory { GetPopularProductsUseCase(get()) }
    factory { GetProductsNearbyUseCase(get()) }

    single { get<AppDatabase>().productDao() }
    single { ProductLocalDataSource(get()) }
    single<ProductProvider> { ProductProviderImpl(get()) }
    single<ProductRepository> { ProductRepositoryImpl(get(), get(), get()) }
}