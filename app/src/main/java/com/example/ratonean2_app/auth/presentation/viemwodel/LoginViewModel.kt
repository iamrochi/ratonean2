package com.example.ratonean2_app.auth.presentation.viemwodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratonean2_app.auth.domain.AuthManager
import com.example.ratonean2_app.auth.domain.provider.AuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authProvider: AuthProvider) : ViewModel() {

    private val _state = MutableStateFlow<String>("")
    val state = _state.asStateFlow()

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                val response = authProvider.loginWithGoogle(idToken)
                AuthManager.currentToken = response.token
                _state.update { "JWT: ${response.token}" }
            } catch (e: Exception) {
                _state.update { "Error: ${e.message}" }
            }
        }
    }
}
