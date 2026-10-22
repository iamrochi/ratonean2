package com.example.ratonean2_app.shoppinglist.presentation.compare

import com.example.ratonean2_app.shoppinglist.domain.usecases.GetAllShoppingListsUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.GetListItemsOnceUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val compareModule = module {
    viewModel {
        CompareListsViewModel(get(), get())
    }
    factory { GetAllShoppingListsUseCase(get()) }
    factory { GetListItemsOnceUseCase(get()) }
}
