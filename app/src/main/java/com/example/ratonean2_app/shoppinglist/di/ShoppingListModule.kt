package com.example.ratonean2_app.shoppinglist.di

import com.example.ratonean2_app.shoppinglist.data.local.ShoppingListLocalDataSource
import com.example.ratonean2_app.shoppinglist.data.repository.ShoppingListRepositoryImpl
import com.example.ratonean2_app.shoppinglist.domain.repository.ShoppingListRepository
import com.example.ratonean2_app.shoppinglist.domain.usecases.AddToActiveListUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.GetAllShoppingListsUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.GetItemsFromListUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.ManageShoppingItemUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.ManageShoppingListUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.SetActiveListUseCase
import com.example.ratonean2_app.shoppinglist.presentation.ShoppingListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val shoppingListModule = module {
    // P0.3/m2: shoppingListDao() ya lo provee databaseModule. No duplicar.
    single { ShoppingListLocalDataSource(get()) }
    single<ShoppingListRepository> { ShoppingListRepositoryImpl(get()) }
    viewModel { ShoppingListViewModel(get(), get(), get(), get(), get()) }

    factory { GetAllShoppingListsUseCase(get()) }
    factory { GetItemsFromListUseCase(get()) }
    factory { AddToActiveListUseCase(get()) }
    factory { SetActiveListUseCase(get()) }
    factory { ManageShoppingItemUseCase(get()) }
    factory { ManageShoppingListUseCase(get()) }
}