package com.example.ratonean2_app

import com.example.ratonean2_app.branch.data.provider.BranchProviderImpl
import com.example.ratonean2_app.branch.domain.model.Branch
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

/**
 * Tests unitarios con MockEngine (offline, sin backend). Patrón base P1.6.
 */
class BranchProviderImplTest {

    private val branchJson = """
        {"branchId":"BR-001","commerceId":"COM-001","flagId":"FLAG-A","name":"Sucursal Test",
         "type":"type","street":"Calle Falsa","number":"123","latitude":1.0,"longitude":1.0,
         "neighborhood":"Vecindario","postalCode":"12345","location":"Ciudad","province":"Provincia"}
    """.trimIndent()

    private fun client(status: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val engine = MockEngine { request ->
            val path = request.url.encodedPath
            val isList = request.method == HttpMethod.Get && path == "/branch"
            when {
                status != HttpStatusCode.OK && isList ->
                    respond("boom", status, headersOf(HttpHeaders.ContentType, "text/plain"))
                request.method == HttpMethod.Get && path == "/branch" ->
                    respond("[$branchJson]", HttpStatusCode.OK, jsonHeaders())
                request.method == HttpMethod.Get && path.startsWith("/branch/") ->
                    if (path.endsWith("/missing")) respond("nf", HttpStatusCode.NotFound, jsonHeaders())
                    else respond(branchJson, HttpStatusCode.OK, jsonHeaders())
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

    private val sample = Branch(
        branchId = "BR-001", commerceId = "COM-001", flagId = "FLAG-A", name = "Sucursal Test",
        type = "type", street = "Calle Falsa", number = "123", latitude = 1.0, longitude = 1.0,
        neighborhood = "Vecindario", postalCode = "12345", location = "Ciudad", province = "Provincia"
    )

    @Test
    fun `getAllBranches Success emite lista`() = runTest {
        val result = BranchProviderImpl(client()).getAllBranches()
            .filterNot { it is NetworkResponse.Loading }.first()
        val data = (result as NetworkResponse.Success<List<Branch>>).data!!
        kotlin.test.assertTrue(data.isNotEmpty(), "La lista mock no debe estar vacía")
    }

    @Test
    fun `getAllBranches Failure en 500 no crashea`() = runTest {
        val result = BranchProviderImpl(client(HttpStatusCode.InternalServerError)).getAllBranches()
            .filterNot { it is NetworkResponse.Loading }.first()
        kotlin.test.assertIs<NetworkResponse.Failure<*>>(result)
    }

    @Test
    fun `getBranchById Success emite item`() = runTest {
        val result = BranchProviderImpl(client()).getBranchById("BR-001")
            .filterNot { it is NetworkResponse.Loading }.first()
        val data = (result as NetworkResponse.Success<Branch>).data!!
        kotlin.test.assertEquals("BR-001", data.branchId)
    }

    @Test
    fun `getBranchById 404 emite Failure`() = runTest {
        val result = BranchProviderImpl(client()).getBranchById("missing")
            .filterNot { it is NetworkResponse.Loading }.first()
        kotlin.test.assertIs<NetworkResponse.Failure<*>>(result)
    }

    @Test
    fun `create update delete Success`() = runTest {
        val provider = BranchProviderImpl(client())
        suspend fun lastOk(flow: Flow<NetworkResponse<Unit>>) =
            flow.filterNot { it is NetworkResponse.Loading }.first()
        kotlin.test.assertIs<NetworkResponse.Success<*>>(lastOk(provider.createBranch(sample)))
        kotlin.test.assertIs<NetworkResponse.Success<*>>(lastOk(provider.updateBranch(sample)))
        kotlin.test.assertIs<NetworkResponse.Success<*>>(lastOk(provider.deleteBranch("BR-001")))
    }
}
