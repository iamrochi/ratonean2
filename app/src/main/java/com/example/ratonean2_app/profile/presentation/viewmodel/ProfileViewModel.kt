package com.example.ratonean2_app.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratonean2_app.auth.data.dto.UpdateUserRequest
import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.user.domain.model.User
import com.example.ratonean2_app.user.domain.usecase.DeleteUserByIdUseCase
import com.example.ratonean2_app.user.domain.usecase.GetUserByIdUseCase
import com.example.ratonean2_app.user.domain.usecase.UpdateUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false
)

class ProfileViewModel(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserByIdUseCase: DeleteUserByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadUser(id: Int) {
        viewModelScope.launch {
            getUserByIdUseCase(id).collect { response ->
                when (response) {
                    is NetworkResponse.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResponse.Success -> _uiState.update { it.copy(isLoading = false, user = response.data) }
                    is NetworkResponse.Failure -> _uiState.update { it.copy(isLoading = false, error = response.error) }
                }
            }
        }
    }

    fun updateUser(id: Int, name: String, email: String) {
        viewModelScope.launch {
            val request = UpdateUserRequest(name, email)
            updateUserUseCase(id, request).collect { response ->
                when (response) {
                    is NetworkResponse.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResponse.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        loadUser(id) // Recargar datos
                    }
                    is NetworkResponse.Failure -> _uiState.update { it.copy(isLoading = false, error = response.error) }
                }
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            deleteUserByIdUseCase(id).collect { response ->
                when (response) {
                    is NetworkResponse.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResponse.Success -> _uiState.update { it.copy(isLoading = false, isDeleted = true) }
                    is NetworkResponse.Failure -> _uiState.update { it.copy(isLoading = false, error = response.error) }
                }
            }
        }
    }
}
