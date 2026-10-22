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
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.coroutines.cancellation.CancellationException

class AuthProviderImpl(private val client: HttpClient): AuthProvider {
    override suspend fun loginWithGoogle(token: String): AuthResponse {
        // P0.5: sin expectSuccess, Ktor no lanza en 4xx. Chequeo manual de status.
        try {
            val response = client.post(ApiUrls.AUTH_GOOGLE) {
                contentType(ContentType.Application.Json)
                setBody(GoogleTokenRequest(token))
            }
            if (!response.status.isSuccess()) {
                throw Exception("Error al loguear (${response.status.value})")
            }
            return response.body<AuthResponse>()
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            if (e is ClientRequestException || e is ServerResponseException) {
                throw Exception("Error al loguear (${e.response.status.value})")
            }
            throw e
        }
    }

    override suspend fun loginWithEmail(
        email: String,
        password: String
    ): AuthResponse {
        // P0.5: antes sin try/catch -> cualquier timeout crasheaba al caller.
        try {
            val response = client.post(ApiUrls.LOGIN) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email.trim(), password))
            }
            if (!response.status.isSuccess()) {
                throw Exception(mapAuthError(response.status.value))
            }
            return response.body<AuthResponse>()
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { throw e }
    }

    override suspend fun createUserWithEmail(name: String, email: String, password: String?) {
        try {
            val response = client.post(ApiUrls.USERS) {
                contentType(ContentType.Application.Json)
                setBody(CreateUserRequest(name.trim(), email.trim(), password))
            }
            if (!response.status.isSuccess()) {
                throw Exception(mapAuthError(response.status.value))
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { throw e }
    }

    private fun mapAuthError(code: Int): String = when (code) {
        400, 422 -> "Datos inválidos. Revisá email y contraseña."
        401 -> "Tu sesión expiró. Iniciá sesión nuevamente."
        404 -> "El recurso solicitado no fue encontrado."
        409 -> "Ese email ya está registrado."
        429 -> "Demasiadas solicitudes. Esperá un momento e intentá de nuevo."
        in 500..599 -> "El servidor tuvo un problema. Intentá de nuevo en unos minutos."
        else -> "Ocurrió un error inesperado ($code)."
    }
}