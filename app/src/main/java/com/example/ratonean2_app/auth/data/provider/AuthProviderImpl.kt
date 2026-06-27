package com.example.ratonean2_app.auth.data.provider

import com.example.ratonean2_app.auth.data.dto.AuthResponse
import com.example.ratonean2_app.auth.data.dto.CreateUserRequest
import com.example.ratonean2_app.auth.data.dto.GoogleTokenRequest
import com.example.ratonean2_app.auth.data.dto.LoginRequest
import com.example.ratonean2_app.auth.domain.provider.AuthProvider
import com.example.ratonean2_app.core.network.ApiUrls
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*

class AuthProviderImpl(private val client: HttpClient): AuthProvider {
    override suspend fun loginWithGoogle(token: String): AuthResponse {
        return try {
            client.post(ApiUrls.AUTH_GOOGLE) {
                contentType(ContentType.Application.Json)
                setBody(GoogleTokenRequest(token))
            }.body<AuthResponse>()
        } catch (e: ClientRequestException) { // 4xx
            val errorText = e.response.bodyAsText()
            throw Exception("Error al loguear: $errorText")
        }
    }

    override suspend fun loginWithEmail(
        email: String,
        password: String
    ): AuthResponse {
        return client.post(ApiUrls.LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body<AuthResponse>()
    }

    override suspend fun createUserWithEmail(name: String, email: String, password: String?) {
        client.post(ApiUrls.USERS) {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(name, email, password))
        }
    }

}