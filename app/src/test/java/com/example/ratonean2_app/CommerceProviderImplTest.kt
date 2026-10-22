package com.example.ratonean2_app

import com.example.ratonean2_app.commerce.data.provider.CommerceProviderImpl
import com.example.ratonean2_app.commerce.domain.model.Commerce
import com.example.ratonean2_app.core.network.NetworkResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

/**
 * Tests unitarios con MockEngine (offline, sin backend).
 * Patrón base P1.6: el engine rutea por método + path.
 */
class CommerceProviderImplTest {

    private val commerceJson = """
        {"id":"COM-001","flagId":"FLAG-A","cuit":"30-12345678-9",
         "businessName":"Empresa de Ejemplo S.A.","flagName":"Visa",
         "flagUrl":"https://example.com/visa.png","sepaVersion":"1.0",
         "lastUpdate":"2025-08-21T10:00:00Z"}
    """.trimIndent()

    private fun client(status: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val engine = MockEngine { request ->
            val path = request.url.encodedPath
            val isList = request.method == HttpMethod.Get && path == "/commerce"
            when {
                // 500 solo para el test de failure (listado).
                status != HttpStatusCode.OK && isList ->
                    respond("boom", status, headersOf(HttpHeaders.ContentType, "text/plain"))
                request.method == HttpMethod.Get && path == "/commerce" ->
                    respond("[$commerceJson]", HttpStatusCode.OK, jsonHeaders())
                request.method == HttpMethod.Get && path.startsWith("/commerce/") ->
                    if (path.endsWith("/missing")) respond("nf", HttpStatusCode.NotFound, jsonHeaders())
                    else respond(commerceJson, HttpStatusCode.OK, jsonHeaders())
                else -> respond("", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "text/plain"))
            }
        }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true })
            }
        }
    }

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private val sample = Commerce(
        id = "COM-001", flagId = "FLAG-A", cuit = "30-12345678-9",
        businessName = "Empresa de Ejemplo S.A.", flagName = "Visa",
        flagUrl = "https://example.com/visa.png", sepaVersion = "1.0",
        lastUpdate = "2025-08-21T10:00:00Z"
    )

    @Test
    fun `getAllCommerces Success emite lista`() = runTest {
        val result = CommerceProviderImpl(client()).getAllCommerces()
            .filterNot { it is NetworkResponse.Loading }.first()
        val data = (result as NetworkResponse.Success<List<Commerce>>).data!!
        kotlin.test.assertTrue(data.isNotEmpty(), "La lista mock no debe estar vacía")
    }

    @Test
    fun `getAllCommerces Failure en 500 no crashea`() = runTest {
        val result = CommerceProviderImpl(client(HttpStatusCode.InternalServerError)).getAllCommerces()
            .filterNot { it is NetworkResponse.Loading }.first()
        kotlin.test.assertIs<NetworkResponse.Failure<*>>(result)
    }

    @Test
    fun `getCommerceById Success emite item`() = runTest {
        val result = CommerceProviderImpl(client()).getCommerceById("COM-001")
            .filterNot { it is NetworkResponse.Loading }.first()
        val data = (result as NetworkResponse.Success<Commerce>).data!!
        kotlin.test.assertEquals("COM-001", data.id)
    }

    @Test
    fun `getCommerceById 404 emite Failure`() = runTest {
        val result = CommerceProviderImpl(client()).getCommerceById("missing")
            .filterNot { it is NetworkResponse.Loading }.first()
        kotlin.test.assertIs<NetworkResponse.Failure<*>>(result)
    }

    @Test
    fun `create update delete Success`() = runTest {
        val provider = CommerceProviderImpl(client())
        suspend fun lastOk(flow: kotlinx.coroutines.flow.Flow<NetworkResponse<Unit>>) =
            flow.filterNot { it is NetworkResponse.Loading }.first()
        kotlin.test.assertIs<NetworkResponse.Success<*>>(lastOk(provider.createCommerce(sample)))
        kotlin.test.assertIs<NetworkResponse.Success<*>>(lastOk(provider.updateCommerce(sample)))
        kotlin.test.assertIs<NetworkResponse.Success<*>>(lastOk(provider.deleteCommerce("COM-001")))
    }
}
