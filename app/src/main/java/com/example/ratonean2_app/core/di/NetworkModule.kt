package com.example.ratonean2_app.core.di

import com.example.ratonean2_app.auth.data.local.SecureTokenStorage
import com.example.ratonean2_app.auth.domain.AuthManager
import com.example.ratonean2_app.core.network.ApiUrls
import com.example.ratonean2_app.core.network.RateLimitException
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
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
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import java.util.UUID


val networkModule = module {
    single {
        // Host propio: solo a este host le mandamos Authorization.
        // Evita fugar el Bearer a Nominatim/OSM u otros hosts externos.
        val ownHost: String? = runCatching {
            io.ktor.http.Url(ApiUrls.BASE_URL).host.lowercase()
        }.getOrNull()

        val client = HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                    isLenient = true
                    coerceInputValues = true
                    explicitNulls = false
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 5_000
                socketTimeoutMillis = 10_000
            }

            // P0.5: retry con backoff solo para errores retryables.
            // No reintenta 400/401/403/404. El Idempotency-Key se setea una vez
            // en HttpSend, así los reintentos reusean la misma key.
            install(HttpRequestRetry) {
                maxRetries = 2
                retryIf { _, response -> response.status == HttpStatusCode.TooManyRequests ||
                        response.status.value in 500..599 }
                retryOnExceptionIf { _, cause ->
                    cause is java.net.SocketTimeoutException ||
                            cause is java.net.ConnectException ||
                            cause is java.net.UnknownHostException
                }
                exponentialDelay(base = 500.0, maxDelayMs = 4_000L)
            }

            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status == HttpStatusCode.TooManyRequests) {
                        val retryAfter = response.headers[HttpHeaders.RetryAfter] ?: "1"
                        throw RateLimitException(
                            "rate_limited:$retryAfter"
                        )
                    }
                    if (response.status == HttpStatusCode.Unauthorized) {
                        // P0.2: limpia memoria + disco y avisa a la UI para ir a login.
                        AuthManager.onUnauthorized()
                        runCatching {
                            GlobalContext.getOrNull()
                                ?.get<SecureTokenStorage>()
                                ?.clear()
                        }
                    }
                }
            }
        }

        client.plugin(HttpSend).intercept { request ->
            val host = request.url.host.lowercase()
            val isOwnHost = ownHost == null || host == ownHost
            val path = request.url.pathSegments.joinToString("/", prefix = "/")
            // P0-fix seguridad: matching exacto + por método. Antes
            // `path.startsWith("/users/")` marcaba como público TODO /users/{id}
            // (GET/PUT/DELETE) y esos iban SIN Authorization.
            // Públicos reales: login/google, POST /users (registro)
            // y catálogo de lectura. /users/by-email REQUIERE auth en el
            // backend (verifica tokenEmail==email), así que SÍ lleva Bearer.
            val isPublic = when {
                path == "/auth/login" || path == "/auth/google" -> true
                path == "/users" && request.method == HttpMethod.Post -> true
                request.method == HttpMethod.Get && path in setOf(
                    "/product/nearby",
                    "/product/searchNearby",
                    "/branch/nearby",
                    "/product/popularProducts"
                ) -> true
                else -> false
            }

            // Nunca mandar Authorization a hosts externos (Nominatim, etc).
            if (isOwnHost && !isPublic) {
                AuthManager.currentToken?.let { token ->
                    request.header(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            // P0.5: Idempotency solo en escrituras con cuerpo. No en DELETE.
            if (!request.headers.contains("Idempotency-Key") &&
                request.method in listOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)
            ) {
                request.header("Idempotency-Key", UUID.randomUUID().toString())
            }

            execute(request)
        }

        client
    }
}
