package com.example.ratonean2_app.auth.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthManager {
    private val _currentToken = MutableStateFlow<String?>(null)
    val currentTokenFlow = _currentToken.asStateFlow()

    var currentToken: String?
        get() = _currentToken.value
        set(value) {
            _currentToken.value = value
        }

    var userId: Int? = null
}


