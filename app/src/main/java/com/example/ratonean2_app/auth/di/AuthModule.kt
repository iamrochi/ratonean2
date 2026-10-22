package com.example.ratonean2_app.auth.di

import com.example.ratonean2_app.auth.data.local.SecureTokenStorage
import com.example.ratonean2_app.auth.data.provider.AuthProviderImpl
import com.example.ratonean2_app.auth.domain.AuthManager
import com.example.ratonean2_app.auth.domain.provider.AuthProvider
import com.example.ratonean2_app.auth.presentation.viemwodel.LoginViewModel
import com.example.ratonean2_app.auth.presentation.viemwodel.RegisterViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single { SecureTokenStorage(androidContext()) }
    // Hidrata el token en memoria al crear el grafo (sobrevive a reinicios).
    single<AuthManagerInitializer> {
        val storage: SecureTokenStorage = get()
        AuthManager.restore(storage.getToken(), storage.getUserId())
        AuthManagerInitializer
    }
    single<AuthProvider> { AuthProviderImpl(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { RegisterViewModel(get()) }
}

object AuthManagerInitializer