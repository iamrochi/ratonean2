package com.example.ratonean2_app.places.data.provider

import com.example.ratonean2_app.core.logging.AppLogger
import com.example.ratonean2_app.core.network.ApiUrls
import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.places.data.local.PlaceSearchCacheDao
import com.example.ratonean2_app.places.data.local.PlaceSearchCacheEntity
import com.example.ratonean2_app.places.domain.model.PlaceResult
import com.example.ratonean2_app.places.domain.provider.PlacesProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private const val TAG = "PlacesProvider"
/** OSM Nominatim policy: ≤1 req/s + caché obligatoria. 30 días TTL. */
private const val PLACES_CACHE_TTL_MS = 30L * 24 * 60 * 60 * 1000
private const val NOMINATIM_MIN_INTERVAL_MS = 1_100L

class PlacesProviderImpl(
    private val client: HttpClient,
    private val cacheDao: PlaceSearchCacheDao? = null
) : PlacesProvider {
    // Rate-limiter en memoria (además del debounce 500ms del ViewModel).
    @Volatile private var lastRequestAt = 0L
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun getPlaces(query: String): NetworkResponse<List<PlaceResult>> {
        val normalized = query.trim().lowercase()
        // Query muy corta = ruido a OSM + payload inútil en 3G. No pegar a red.
        if (normalized.length < 3) return NetworkResponse.Success(emptyList())
        return try {
            // 1) Caché 30d primero (offline-first: buscador funciona sin red).
            val dao = cacheDao
            if (dao != null) {
                runCatching {
                    val cachedJson = dao.getResponse(normalized)
                    val updatedAt = dao.getUpdatedAt(normalized)
                    if (cachedJson != null && updatedAt != null &&
                        System.currentTimeMillis() - updatedAt < PLACES_CACHE_TTL_MS
                    ) {
                        val cached = json.decodeFromString(
                            ListSerializer(PlaceResult.serializer()), cachedJson
                        )
                        AppLogger.d(TAG, "Cache hit places '$normalized' (${cached.size})")
                        return NetworkResponse.Success(cached)
                    } else if (cachedJson != null) {
                        // TTL vencido pero sin red después igual sirve: se devuelve
                        // abajo como fallback si la red falla.
                    }
                }.onFailure { AppLogger.w(TAG, "Places cache read falló: ${it.message}") }
            }

            // 2) Rate-limit cliente: nunca más de 1 req/s a Nominatim.
            val now = System.currentTimeMillis()
            val elapsed = now - lastRequestAt
            if (elapsed < NOMINATIM_MIN_INTERVAL_MS) {
                kotlinx.coroutines.delay(NOMINATIM_MIN_INTERVAL_MS - elapsed)
            }
            // P0.5: Nominatim exige User-Agent identificable + no recibe nuestro Bearer
            // (NetworkModule ya no manda Authorization a hosts externos).
            val response = client.get(ApiUrls.PLACES_URL) {
                header("User-Agent", "RatoneanApp/1.0 (contacto@ratonean.com)")
                header("Accept", "application/json")
                parameter("q", query.trim())
                parameter("format", "json")
                parameter("addressdetails", 1)
                parameter("limit", 5)
                parameter("countrycodes", "ar")
            }
            lastRequestAt = System.currentTimeMillis()

            if (response.status.isSuccess()) {
                val places = response.body<List<PlaceResult>>()
                // 3) Guardar en caché (evicción por antigüedad, fire-and-forget).
                val dao2 = cacheDao
                if (dao2 != null) {
                    runCatching {
                        dao2.upsert(
                            PlaceSearchCacheEntity(
                                query = normalized,
                                responseJson = json.encodeToString(
                                    ListSerializer(PlaceResult.serializer()), places
                                )
                            )
                        )
                        dao2.pruneOlderThan(System.currentTimeMillis() - PLACES_CACHE_TTL_MS)
                    }.onFailure { AppLogger.w(TAG, "Places cache write falló: ${it.message}") }
                }
                NetworkResponse.Success(places)
            } else {
                // Fallback: caché vencida mejor que nada.
                staleFallback(normalized)
                    ?: NetworkResponse.Failure("Error: ${response.status.value}")
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            AppLogger.w(TAG, "Places network falló, probando caché: ${e.message}")
            staleFallback(normalized) ?: NetworkResponse.Failure(e.message ?: "Unknown error")
        }
    }

    private suspend fun staleFallback(normalized: String): NetworkResponse<List<PlaceResult>>? {
        val dao = cacheDao ?: return null
        return runCatching {
            val stale = dao.getResponse(normalized) ?: return null
            val places = json.decodeFromString(ListSerializer(PlaceResult.serializer()), stale)
            AppLogger.d(TAG, "Places stale fallback '$normalized' (${places.size})")
            NetworkResponse.Success(places)
        }.getOrNull()
    }
}