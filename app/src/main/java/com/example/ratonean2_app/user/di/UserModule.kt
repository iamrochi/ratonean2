package com.example.ratonean2_app.user.di

import com.example.ratonean2_app.user.data.local.UserPreferencesManager
import com.example.ratonean2_app.user.data.provider.UserProviderImpl
import com.example.ratonean2_app.user.data.repository.UserRepositoryImpl
import com.example.ratonean2_app.user.domain.provider.UserProvider
import com.example.ratonean2_app.user.domain.repository.UserRepository
import com.example.ratonean2_app.user.domain.usecase.DeleteUserByIdUseCase
import com.example.ratonean2_app.user.domain.usecase.GetUserByEmailUseCase
import com.example.ratonean2_app.user.domain.usecase.GetUserByIdUseCase
import com.example.ratonean2_app.user.domain.usecase.GetUserPrefsLocationUseCase
import com.example.ratonean2_app.user.domain.usecase.SaveUserPrefsLocationUseCase
import com.example.ratonean2_app.user.domain.usecase.UpdateUserUseCase
import com.example.ratonean2_app.profile.presentation.viewmodel.ProfileViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val userModule = module {
    single<UserProvider> { UserProviderImpl(get()) }
    factory { GetUserByIdUseCase(get()) }
    factory { GetUserByEmailUseCase(get()) }
    factory { DeleteUserByIdUseCase(get()) }
    factory { UpdateUserUseCase(get()) }

    viewModel { ProfileViewModel(get(), get(), get(), get()) }

    single { UserPreferencesManager(androidContext()) }
    single<UserRepository> { UserRepositoryImpl(get()) }

    factory { GetUserPrefsLocationUseCase(get()) }
    factory { SaveUserPrefsLocationUseCase(get()) }

}