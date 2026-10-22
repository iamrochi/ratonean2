package com.example.ratonean2_app

import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.product.data.provider.ProductProviderImpl
import com.example.ratonean2_app.product.domain.model.Product
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

/**
 * Tests unitarios con MockEngine (offline, sin backend). Patrón base P1.6.
 * Reemplaza los viejos tests de integración que exigían API real.
 */
class ProductProviderImplTest {

    private val productJson = """
        {"commerceId":"COM-001","flagId":"FLAG-A","branchId":"BR-001","productId":"P-1",
         "ean":"7791234567890","description":"Leche Entera","presentationQuantity":1.0,
         "presentationUnit":"Lts","brand":"La Lechera","listPrice":1500.0,
         "referencePrice":1450.0,"referenceQuantity":1.0,"referenceUnit":"Lts"}
    """.trimIndent()

    private fun client(status: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val engine = MockEngine { request ->
            val path = request.url.encodedPath
            val isList = request.method == HttpMethod.Get && path == "/product"
            when {
                status != HttpStatusCode.OK && isList ->
                    respond("boom", status, headersOf(HttpHeaders.ContentType, "text/plain"))
                request.method == HttpMethod.Get && path == "/product" ->
                    respond("[$productJson]", HttpStatusCode.OK, jsonHeaders())
                request.method == HttpMethod.Get && path.startsWith("/product/") ->
                    if (path.endsWith("/missing")) respond("nf", HttpStatusCode.NotFound, jsonHeaders())
                    else respond(productJson, HttpStatusCode.OK, jsonHeaders())
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

    private val sample = Product(
        commerceId = "COM-001", flagId = "FLAG-A", branchId = "BR-001", productId = "P-1",
        ean = "7791234567890", description = "Leche Entera", presentationQuantity = 1.0,
        presentationUnit = "Lts", brand = "La Lechera", listPrice = 1500.0,
        referencePrice = 1450.0, referenceQuantity = 1.0, referenceUnit = "Lts"
    )

    @Test
    fun `getAllProducts Success emite lista`() = runTest {
        val result = ProductProviderImpl(client()).getAllProducts()
            .filterNot { it is NetworkResponse.Loading }.first()
        val data = (result as NetworkResponse.Success<List<Product>>).data!!
        kotlin.test.assertTrue(data.isNotEmpty(), "La lista mock no debe estar vacía")
    }

    @Test
    fun `getAllProducts Failure en 500 no crashea`() = runTest {
        val result = ProductProviderImpl(client(HttpStatusCode.InternalServerError)).getAllProducts()
            .filterNot { it is NetworkResponse.Loading }.first()
        kotlin.test.assertIs<NetworkResponse.Failure<*>>(result)
    }

    @Test
    fun `getProductById Success emite item`() = runTest {
        val result = ProductProviderImpl(client()).getProductById("P-1")
            .filterNot { it is NetworkResponse.Loading }.first()
        val data = (result as NetworkResponse.Success<Product>).data!!
        kotlin.test.assertEquals("P-1", data.productId)
    }

    @Test
    fun `getProductById 404 emite Failure`() = runTest {
        val result = ProductProviderImpl(client()).getProductById("missing")
            .filterNot { it is NetworkResponse.Loading }.first()
        kotlin.test.assertIs<NetworkResponse.Failure<*>>(result)
    }

    @Test
    fun `create update delete Success`() = runTest {
        val provider = ProductProviderImpl(client())
        suspend fun lastOk(flow: Flow<NetworkResponse<Unit>>) =
            flow.filterNot { it is NetworkResponse.Loading }.first()
        kotlin.test.assertIs<NetworkResponse.Success<*>>(lastOk(provider.createProduct(sample)))
        kotlin.test.assertIs<NetworkResponse.Success<*>>(lastOk(provider.updateProduct(sample)))
        kotlin.test.assertIs<NetworkResponse.Success<*>>(lastOk(provider.deleteProduct("P-1")))
    }
}
