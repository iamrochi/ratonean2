package com.example.ratonean2_app.product.data.provider

import com.example.ratonean2_app.core.network.ApiUrls
import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.core.network.paginate
import com.example.ratonean2_app.product.data.dto.BranchWithProductsResponse
import com.example.ratonean2_app.product.domain.helper.appendParams
import com.example.ratonean2_app.product.domain.model.Product
import com.example.ratonean2_app.product.domain.provider.ProductProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.URLEncoder
import kotlin.coroutines.cancellation.CancellationException

class ProductProviderImpl(
    private val client: HttpClient,
    private val pendingWritesDao: com.example.ratonean2_app.core.data.local.PendingWritesDao? = null
) : ProductProvider {
    override fun getAllProducts(limit: Int, offset: Int): Flow<NetworkResponse<List<Product>>> = flow {
        emit(NetworkResponse.Loading())
        try {
            val response = client.get(ApiUrls.PRODUCTS) {
                paginate(limit, offset)
            }
            if (response.status.isSuccess()) {
                val products = response.body<List<Product>>()
                emit(NetworkResponse.Success(products))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun getProductById(id: String): Flow<NetworkResponse<Product>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.get(ApiUrls.PRODUCTS_BY_ID.replace("{id}", id))
            if (response.status.isSuccess()) {
                val product = response.body<Product>()
                emit(NetworkResponse.Success(product))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun createProduct(product: Product): Flow<NetworkResponse<Unit>> = flow {
       try {
           emit(NetworkResponse.Loading())
           val response = client.post(ApiUrls.PRODUCTS) {
               contentType(ContentType.Application.Json)
               setBody(product)
           }
           if (response.status.isSuccess()) {
               emit(NetworkResponse.Success(Unit))
           } else {
               emit(NetworkResponse.Failure(response.status.description))
           }
        } catch (e: CancellationException) { throw e } catch (e: Exception){
           if (enqueueOffline("POST", "/product", product)) {
               emit(NetworkResponse.Failure("Sin conexión — se enviará cuando haya red."))
           } else {
               emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
           }
       }
    }

    override fun updateProduct(product: Product): Flow<NetworkResponse<Unit>> = flow{
        try {
            emit(NetworkResponse.Loading())
            val response = client.put(ApiUrls.PRODUCTS_BY_ID.replace("{id}", product.productId)) {
                contentType(ContentType.Application.Json)
                setBody(product)
            }
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(Unit))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            if (enqueueOffline("PUT", "/product/${product.productId}", product)) {
                emit(NetworkResponse.Failure("Sin conexión — se enviará cuando haya red."))
            } else {
                emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
            }
        }
    }

    override fun deleteProduct(id: String): Flow<NetworkResponse<Unit>> = flow{
        try {
            emit(NetworkResponse.Loading())
            val response = client.delete(ApiUrls.PRODUCTS_BY_ID.replace("{id}", id))
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(Unit))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
       } catch (e: CancellationException) { throw e } catch (e: Exception){
           if (enqueueOffline("DELETE", "/product/$id", null)) {
               emit(NetworkResponse.Failure("Sin conexión — se eliminará cuando haya red."))
           } else {
               emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
           }
       }
    }

    override fun getProductsByBranch(
        branchId: String,
        limit: Int,
        offset: Int
    ): Flow<NetworkResponse<List<Product>>> = flow {
        emit(NetworkResponse.Loading())
        try {
            val response = client.get(ApiUrls.PRODUCTS_BY_BRANCH.replace("{branchId}", branchId)) {
                paginate(limit, offset)
            }
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(response.body()))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun getProductsByBranchKey(
        branchKey: String,
        limit: Int,
        offset: Int
    ): Flow<NetworkResponse<List<Product>>> = flow {
        emit(NetworkResponse.Loading())
        try {
            val encoded = URLEncoder.encode(branchKey, "UTF-8")
            val response = client.get(ApiUrls.PRODUCTS_BY_BRANCH_KEY.replace("{key}", encoded)) {
                paginate(limit, offset)
            }
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(response.body()))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun getProductsWithPromos(
        branchId: String,
        brand: String?,
        limit: Int,
        offset: Int
    ): Flow<NetworkResponse<List<Product>>> = flow {
        emit(NetworkResponse.Loading())
        try {
            val url = ApiUrls.PRODUCTS_PROMOS.appendParams(mapOf("branchId" to branchId, "brand" to brand))
            val response = client.get(url) {
                paginate(limit, offset)
            }
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(response.body()))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun getProductsBySearchInBranches(
        branchIds: List<String>,
        query: String,
        limit: Int,
        offset: Int
    ): Flow<NetworkResponse<List<Product>>> = flow {
        emit(NetworkResponse.Loading())
        try {
            val idsParam = branchIds.joinToString(",")
            val url = ApiUrls.PRODUCTS_BY_SEARCH_IN_BRANCHES.appendParams(
                mapOf("branchIds" to idsParam, "q" to query)
            )
            val response = client.get(url) {
                paginate(limit, offset)
            }
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(response.body()))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun getProductsBySearchInBranchKeys(
        branchKeys: List<String>,
        query: String,
        limit: Int,
        offset: Int
    ): Flow<NetworkResponse<List<Product>>> = flow {
        emit(NetworkResponse.Loading())
        try {
            val url = ApiUrls.PRODUCTS_BY_SEARCH_IN_BRANCH_KEYS.appendParams(
                mapOf("branchKeys" to branchKeys.joinToString(","), "q" to query)
            )
            val response = client.get(url) {
                paginate(limit, offset)
            }
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(response.body()))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun getPopularProducts(
        branchIds: List<String>,
        limit: Int?,
        offset: Int
    ): Flow<NetworkResponse<List<Product>>> = flow {
        emit(NetworkResponse.Loading())
        try {
            val idsParam = branchIds.joinToString(",")
            val params = mutableMapOf("branchIds" to idsParam)
            limit?.let { params["limitPerKeyword"] = it.toString() }
            val url = ApiUrls.PRODUCTS_POPULAR.appendParams(params)
            val response = client.get(url) {
                paginate(offset = offset)
            }
            // P0.5: antes emitía Success sin chequear status.
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(response.body()))
            } else {
                emit(NetworkResponse.Failure("HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message))
        }

    }

    override fun getProductsNearby(
        latitude: Double,
        longitude: Double,
        maxDistance: Double?,
        limit: Int,
        offset: Int
    ): Flow<NetworkResponse<List<BranchWithProductsResponse>>> = flow {
        emit(NetworkResponse.Loading())
        try {
            val params = mutableMapOf(
                "lat" to latitude.toString(),
                "lon" to longitude.toString()
            )
            maxDistance?.let { params["radius"] = it.toString() }
            val url = ApiUrls.PRODUCTS_NEARBY.appendParams(params)
            val response = client.get(url) {
                paginate(limit, offset)
            }
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(response.body()))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    /**
     * Outbox offline-first: si no hay red, guarda la escritura con su
     * Idempotency-Key para que OutboxSyncWorker la reintente. Devuelve true
     * si se encoló (el caller emite "se enviará con red").
     */
    private suspend fun enqueueOffline(method: String, path: String, product: Product?): Boolean {
        val dao = pendingWritesDao ?: return false
        return try {
            val body = product?.let {
                kotlinx.serialization.json.Json.encodeToString(
                    Product.serializer(), it
                )
            }
            dao.enqueue(
                com.example.ratonean2_app.core.data.local.PendingWriteEntity(
                    method = method,
                    path = path,
                    bodyJson = body,
                    idempotencyKey = java.util.UUID.randomUUID().toString()
                )
            )
            com.example.ratonean2_app.core.work.OutboxSyncScheduler.scheduleNow()
            true
        } catch (_: Exception) {
            false
        }
    }
}
