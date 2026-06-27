package com.example.ratonean2_app.core.di

import com.example.ratonean2_app.auth.domain.AuthManager
import com.example.ratonean2_app.core.network.RateLimitException
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import java.util.UUID


val networkModule = module {
    single {
        val client = HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 5_000
                socketTimeoutMillis = 10_000
            }

            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status == HttpStatusCode.TooManyRequests) {
                        throw RateLimitException(
                            "⚠️ Demasiadas peticiones. Reintenta después de ${response.headers[HttpHeaders.RetryAfter] ?: "1"} seg."
                        )
                    }
                    if (response.status == HttpStatusCode.Unauthorized) {
                        AuthManager.currentToken = null
                    }
                }
            }
        }

        client.plugin(HttpSend).intercept { request ->
            val path = request.url.pathSegments.joinToString("/", prefix = "/")
            val publicPaths = setOf(
                "/auth/login",
                "/auth/google",
                "/users",
                "/product/nearby",
                "/branch/nearby",
                "/product/popularProducts"
            )
            val isPublic = path in publicPaths

            if (!isPublic) {
                AuthManager.currentToken?.let { token ->
                    request.header(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            if (request.method in listOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Delete, HttpMethod.Patch)) {
                request.header("Idempotency-Key", UUID.randomUUID().toString())
            }

            execute(request)
        }

        client
    }
}
