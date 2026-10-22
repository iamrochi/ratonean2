package com.example.ratonean2_app.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratonean2_app.auth.data.dto.UpdateUserRequest
import com.example.ratonean2_app.auth.data.local.SecureTokenStorage
import com.example.ratonean2_app.auth.domain.AuthManager
import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.core.network.toUserFriendlyMessage
import com.example.ratonean2_app.user.domain.model.User
import com.example.ratonean2_app.user.domain.usecase.DeleteUserByIdUseCase
import com.example.ratonean2_app.user.domain.usecase.GetUserByIdUseCase
import com.example.ratonean2_app.user.domain.usecase.UpdateUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false,
    val isLoggedOut: Boolean = false
)

class ProfileViewModel(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserByIdUseCase: DeleteUserByIdUseCase,
    private val tokenStorage: SecureTokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadUser(id: Int) {
        viewModelScope.launch {
            getUserByIdUseCase(id).collect { response ->
                when (response) {
                    is NetworkResponse.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is NetworkResponse.Success -> _uiState.update { it.copy(isLoading = false, user = response.data) }
                    is NetworkResponse.Failure -> _uiState.update {
                        it.copy(isLoading = false, error = response.error.toUserFriendlyMessage())
                    }
                }
            }
        }
    }

    fun updateUser(id: Int, name: String, email: String) {
        viewModelScope.launch {
            val request = UpdateUserRequest(name, email)
            updateUserUseCase(id, request).collect { response ->
                when (response) {
                    is NetworkResponse.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is NetworkResponse.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        loadUser(id)
                    }
                    is NetworkResponse.Failure -> _uiState.update {
                        it.copy(isLoading = false, error = response.error.toUserFriendlyMessage())
                    }
                }
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            deleteUserByIdUseCase(id).collect { response ->
                when (response) {
                    is NetworkResponse.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is NetworkResponse.Success -> _uiState.update { it.copy(isLoading = false, isDeleted = true) }
                    is NetworkResponse.Failure -> _uiState.update {
                        it.copy(isLoading = false, error = response.error.toUserFriendlyMessage())
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /** Cierra sesión: limpia token cifrado + memoria. La UI muestra snackbar. */
    fun logout() {
        viewModelScope.launch {
            try {
                tokenStorage.clear()
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { /* limpiar memoria igual */ }
            AuthManager.onUnauthorized()
            _uiState.update { it.copy(user = null, isLoggedOut = true) }
        }
    }

    fun consumeLogout() {
        _uiState.update { it.copy(isLoggedOut = false) }
    }
}
