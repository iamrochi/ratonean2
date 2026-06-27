package com.example.ratonean2_app.auth.domain.provider

import com.example.ratonean2_app.auth.data.dto.AuthResponse

interface AuthProvider {
    suspend fun loginWithGoogle(token: String): AuthResponse
    suspend fun loginWithEmail(email: String, password: String): AuthResponse
    suspend fun createUserWithEmail(name: String, email: String, password:String?)
}
