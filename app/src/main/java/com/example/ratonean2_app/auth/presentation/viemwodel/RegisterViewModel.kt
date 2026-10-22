package com.example.ratonean2_app.auth.presentation.viemwodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratonean2_app.auth.domain.provider.AuthProvider
import com.example.ratonean2_app.core.network.toUserFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class RegisterViewModel(
    private val authProvider: AuthProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }

    fun onNameChange(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName)
    }

    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword)
    }

    fun registerUser() {
        val name = _uiState.value.name.trim()
        val email = _uiState.value.email.trim()
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Ingresá tu nombre.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(error = "Ingresá un email válido.")
            return
        }
        val password = _uiState.value.password
        if (password.length < 8) {
            _uiState.value = _uiState.value.copy(error = "La contraseña debe tener al menos 8 caracteres")
            return
        }
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                authProvider.createUserWithEmail(
                    name = name,
                    email = email,
                    password = password
                )
                _uiState.value = _uiState.value.copy(isLoading = false, success = true)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message.toUserFriendlyMessage())
            }
        }
    }
}
