package com.example.ratonean2_app.commerce.data.repository

import android.util.Log
import com.example.ratonean2_app.commerce.data.entity.toDomain
import com.example.ratonean2_app.commerce.data.entity.toEntity
import com.example.ratonean2_app.commerce.data.local.CommerceLocalDataSource
import com.example.ratonean2_app.commerce.domain.model.Commerce
import com.example.ratonean2_app.commerce.domain.provider.CommerceProvider
import com.example.ratonean2_app.commerce.domain.repository.CommerceRepository
import com.example.ratonean2_app.core.network.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.cancellation.CancellationException

class CommerceRepositoryImpl(
    private val localDataSource: CommerceLocalDataSource,
    private val remoteProvider: CommerceProvider
) : CommerceRepository {

    private val TAG = "CommerceRepository"
    private val CACHE_EXPIRATION_MS = 30 * 60 * 1000L // P0.5/M4: TTL como Branch.

    override fun getAllCommerces(): Flow<List<Commerce>> = flow {
        var hasLocal = false
        try {
            val entities = localDataSource.getAllCommerces().first()
            val local = entities.map { it.toDomain() }
            if (local.isNotEmpty()) {
                Log.d(TAG, "📦 Room: ${local.size} comercios")
                emit(local)
                hasLocal = true
                val mostRecent = entities.maxOfOrNull { it.cacheUpdated } ?: 0L
                if (System.currentTimeMillis() - mostRecent < CACHE_EXPIRATION_MS) {
                    Log.d(TAG, "✅ Cache fresh, saltando API")
                    return@flow
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "❌ Error Room: ${e.message}", e)
        }

        remoteProvider.getAllCommerces().collect { response ->
            if (response is NetworkResponse.Success) {
                val remote = response.data.orEmpty()
                if (remote.isNotEmpty()) {
                    runCatching {
                        localDataSource.insertCommerces(remote.map { it.toEntity() })
                    }.onFailure { Log.e(TAG, "Error persistiendo: ${it.message}", it) }
                    emit(remote)
                }
            } else if (response is NetworkResponse.Failure) {
                Log.w(TAG, "⚠️ API Failure: ${response.error}")
                if (!hasLocal) emit(emptyList())
            }
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        Log.e(TAG, "Error getAllCommerces: ${e.message}", e)
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    override fun getCommerceById(id: String): Flow<Commerce?> = flow<Commerce?> {
        val local = localDataSource.getCommerceById(id).first()?.toDomain()
        if (local != null) emit(local)

        remoteProvider.getCommerceById(id).collect { response ->
            if (response is NetworkResponse.Success) {
                response.data?.let {
                    runCatching { localDataSource.insertCommerce(it.toEntity()) }
                        .onFailure { e -> Log.e(TAG, "Error persistiendo: ${e.message}", e) }
                    emit(it)
                }
            } else if (response is NetworkResponse.Failure) {
                Log.w(TAG, "getCommerceById Failure: ${response.error}")
            }
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        Log.e(TAG, "❌ Error getCommerceById: ${e.message}", e)
        emit(null)
    }.flowOn(Dispatchers.IO)
}