package com.example.ratonean2_app.auth.presentation.viemwodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratonean2_app.auth.data.provider.AuthProviderImpl
import com.example.ratonean2_app.auth.domain.provider.AuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
    val uiState: StateFlow<RegisterUiState> = _uiState

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
        val password = _uiState.value.password
        if (password.length < 8) {
            _uiState.value = _uiState.value.copy(error = "La contraseña debe tener al menos 8 caracteres")
            return
        }
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                authProvider.createUserWithEmail(
                    name = _uiState.value.name,
                    email = _uiState.value.email,
                    password = password
                )
                _uiState.value = _uiState.value.copy(isLoading = false, success = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
