package com.example.ratonean2_app.commerce.data.provider

import com.example.ratonean2_app.commerce.domain.model.Commerce
import com.example.ratonean2_app.commerce.domain.provider.CommerceProvider
import com.example.ratonean2_app.core.network.ApiUrls
import com.example.ratonean2_app.core.network.NetworkResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

class CommerceProviderImpl(private val client: HttpClient): CommerceProvider {
    override fun getAllCommerces(): Flow<NetworkResponse<List<Commerce>>> = flow {
        try {
            emit(NetworkResponse.Loading())
            // P0.5: pagina por defecto para no traer la tabla entera a memoria.
            val response = client.get(ApiUrls.COMMERCES) {
                parameter("limit", 50)
                parameter("offset", 0)
            }
            if (response.status.isSuccess()) {
                val commerces = response.body<List<Commerce>>()
                emit(NetworkResponse.Success(commerces))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun getCommerceById(id: String): Flow<NetworkResponse<Commerce>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.get(ApiUrls.COMMERCES_BY_ID.replace("{id}", id))
            if (response.status.isSuccess()) {
             val commerce = response.body<Commerce>()
             emit(NetworkResponse.Success(commerce))
            }else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun createCommerce(commerce: Commerce): Flow<NetworkResponse<Unit>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.post(ApiUrls.COMMERCES) {
                contentType(ContentType.Application.Json)
                setBody(commerce)
            }
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(Unit))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun updateCommerce(commerce: Commerce): Flow<NetworkResponse<Unit>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.put(ApiUrls.COMMERCES_BY_ID.replace("{id}", commerce.id)) {
                contentType(ContentType.Application.Json)
                setBody(commerce)
            }
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(Unit))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun deleteCommerce(id: String): Flow<NetworkResponse<Unit>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.delete(ApiUrls.COMMERCES_BY_ID.replace("{id}", id))
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(Unit))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }
}