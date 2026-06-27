package com.example.ratonean2_app.user.domain.provider

import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.user.domain.model.User
import kotlinx.coroutines.flow.Flow

import com.example.ratonean2_app.auth.data.dto.UpdateUserRequest

interface UserProvider {
    //fun createNewUser is created in AuthProvider
    fun getUserById(id: Int): Flow<NetworkResponse<User>>
    fun getUserByEmail(email: String): Flow<NetworkResponse<User>>
    fun deleteUser(id: Int): Flow<NetworkResponse<Unit>>
    fun updateUser(id: Int, userUpdate: UpdateUserRequest): Flow<NetworkResponse<Unit>>
}