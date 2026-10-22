package com.example.ratonean2_app.auth.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthManager {
    private val _currentToken = MutableStateFlow<String?>(null)
    val currentTokenFlow: StateFlow<String?> = _currentToken.asStateFlow()

    var currentToken: String?
        get() = _currentToken.value
        set(value) {
            _currentToken.value = value
        }

    var userId: Int? = null

    // P0.2: evento de sesión expirada. La UI lo colecta para navegar a login.
    // Se emite desde NetworkModule cuando el backend devuelve 401.
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    fun onUnauthorized() {
        currentToken = null
        userId = null
        _sessionExpired.tryEmit(Unit)
    }

    /** Hidratación al arrancar la app desde SecureTokenStorage (ver AuthModule). */
    fun restore(token: String?, restoredUserId: Int?) {
        currentToken = token
        userId = restoredUserId
    }
}


