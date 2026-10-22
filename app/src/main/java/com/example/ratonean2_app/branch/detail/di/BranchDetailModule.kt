package com.example.ratonean2_app.branch.detail.di

import com.example.ratonean2_app.branch.detail.presentation.viewmodel.BranchDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val branchDetailModule = module {
    viewModel { BranchDetailViewModel(get(), get()) }
}
