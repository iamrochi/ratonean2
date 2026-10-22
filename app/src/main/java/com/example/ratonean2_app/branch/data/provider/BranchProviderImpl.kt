package com.example.ratonean2_app.branch.data.provider

import com.example.ratonean2_app.branch.domain.model.Branch
import com.example.ratonean2_app.branch.domain.provider.BranchProvider
import com.example.ratonean2_app.core.logging.AppLogger
import com.example.ratonean2_app.core.network.ApiUrls
import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.core.network.paginate
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

class BranchProviderImpl(private val client: HttpClient) : BranchProvider {

    private val TAG = "BranchProvider"

    override fun getAllBranches(limit: Int, offset: Int): Flow<NetworkResponse<List<Branch>>> = flow {
        emit(NetworkResponse.Loading())
        try {
            val response = client.get(ApiUrls.BRANCHES) {
                paginate(limit, offset)
            }
            if (response.status.isSuccess()) {
                val branches = response.body<List<Branch>>()
                emit(NetworkResponse.Success(branches))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun getBranchById(id: String): Flow<NetworkResponse<Branch>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.get(ApiUrls.BRANCHES_BY_ID.replace("{id}", id))
            if (response.status.isSuccess()) {
                val branch = response.body<Branch>()
                emit(NetworkResponse.Success(branch))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun getNearbyBranches(
        latitude: Double,
        longitude: Double,
        distance: Double,
        limit: Int,
        offset: Int
    ): Flow<NetworkResponse<List<Branch>>> = flow {
        try {
            emit(NetworkResponse.Loading())
            // P1.5: AppLogger no escribe en release (evita PII lat/lon en logcat).
            AppLogger.d(TAG, "GET ${ApiUrls.BRANCHES_NEARBY} (radius=$distance)")
            val response = client.get(ApiUrls.BRANCHES_NEARBY) {
                parameter("lat", latitude)
                parameter("lon", longitude)
                parameter("radius", distance)
                paginate(limit, offset)
            }
            AppLogger.d(TAG, "Response status: ${response.status}")
            if (response.status.isSuccess()) {
                val branches = response.body<List<Branch>>()
                AppLogger.d(TAG, "Body: ${branches.size} sucursales")
                emit(NetworkResponse.Success(branches))
            } else {
                AppLogger.e(TAG, "Error status: ${response.status.description}")
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            AppLogger.e(TAG, "Exception: ${e.message}", e)
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun createBranch(branch: Branch): Flow<NetworkResponse<Unit>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.post(ApiUrls.BRANCHES) {
                contentType(ContentType.Application.Json)
                setBody(branch)
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

    override fun updateBranch(branch: Branch): Flow<NetworkResponse<Unit>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.put(ApiUrls.BRANCHES_BY_ID.replace("{id}", branch.branchId)) {
                contentType(ContentType.Application.Json)
                setBody(branch)
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

    override fun deleteBranch(id: String): Flow<NetworkResponse<Unit>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.delete(ApiUrls.BRANCHES_BY_ID.replace("{id}", id))
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
