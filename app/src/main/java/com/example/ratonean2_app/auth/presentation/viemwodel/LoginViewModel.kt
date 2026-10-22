package com.example.ratonean2_app.auth.presentation.viemwodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratonean2_app.auth.data.local.SecureTokenStorage
import com.example.ratonean2_app.auth.domain.AuthManager
import com.example.ratonean2_app.auth.domain.provider.AuthProvider
import com.example.ratonean2_app.core.network.toUserFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class LoginViewModel(
    private val authProvider: AuthProvider,
    private val tokenStorage: SecureTokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authProvider.loginWithGoogle(idToken)
                tokenStorage.saveToken(response.token, response.userId)
                AuthManager.restore(response.token, response.userId)
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message.toUserFriendlyMessage())
                }
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        // P0 validación mínima antes de pegar a red (Patterns, no contains).
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.update { it.copy(error = "Ingresá un email válido.") }
            return
        }
        if (password.length < 8) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 8 caracteres.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authProvider.loginWithEmail(cleanEmail, password)
                tokenStorage.saveToken(response.token, response.userId)
                AuthManager.restore(response.token, response.userId)
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message.toUserFriendlyMessage())
                }
            }
        }
    }

    fun logout() {
        tokenStorage.clear()
        AuthManager.onUnauthorized()
        _uiState.update { LoginUiState() }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
