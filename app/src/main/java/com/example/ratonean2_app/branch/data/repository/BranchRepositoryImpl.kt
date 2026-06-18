package com.example.ratonean2_app.branch.data.repository

import android.util.Log
import com.example.ratonean2_app.branch.data.entity.toDomain
import com.example.ratonean2_app.branch.data.local.BranchLocalDataSource
import com.example.ratonean2_app.branch.domain.model.Branch
import com.example.ratonean2_app.branch.domain.model.toEntity
import com.example.ratonean2_app.branch.domain.provider.BranchProvider
import com.example.ratonean2_app.branch.domain.repository.BranchRepository
import com.example.ratonean2_app.core.network.NetworkResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class BranchRepositoryImpl(
    private val localDataSource: BranchLocalDataSource,
    private val remoteProvider: BranchProvider
) : BranchRepository {

    private val TAG = "BranchRepository"
    private val CACHE_EXPIRATION_MS = 30 * 60 * 1000 // 30 minutos para sucursales

    private fun isCacheFresh(lastUpdated: Long?): Boolean {
        if (lastUpdated == null) return false
        val diff = System.currentTimeMillis() - lastUpdated
        return diff < CACHE_EXPIRATION_MS
    }

    override fun getNearbyBranches(
        lat: Double,
        lon: Double,
        distance: Double,
        limit: Int,
        offset: Int
    ): Flow<List<Branch>> = flow {
        Log.d(TAG, "🔍 Buscando sucursales cerca de: $lat, $lon (radio: ${distance}km)")

        val localEntities = localDataSource.getNearbyBranches(lat, lon, distance).first()
        val localBranches = localEntities.map { it.toDomain() }

        if (localBranches.isNotEmpty()) {
            Log.d(TAG, "📦 Room: Despachando ${localBranches.size} sucursales")
            emit(localBranches)
            
            val mostRecent = localEntities.maxOfOrNull { it.lastUpdated } ?: 0L
            if (isCacheFresh(mostRecent)) {
                Log.d(TAG, "✅ Cache fresh (${(System.currentTimeMillis() - mostRecent)/1000}s), saltando API")
                return@flow
            }
        } else {
            Log.d(TAG, "⚪ Room: No hay sucursales guardadas en este radio")
        }

        Log.d(TAG, "🌐 API: Consultando sucursales cercanas...")
        remoteProvider.getNearbyBranches(lat, lon, distance, limit, offset).collect { response ->
            when (response) {
                is NetworkResponse.Success -> {
                    val remoteBranches = response.data.orEmpty()
                    Log.d(TAG, "🌐 API Success: Recibidas ${remoteBranches.size} sucursales")
                    if (remoteBranches.isNotEmpty()) {
                        try {
                            localDataSource.insertBranches(remoteBranches.map { it.toEntity() })
                            emit(remoteBranches)
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error al persistir API: ${e.message}")
                            emit(remoteBranches)
                        }
                    }
                }
                is NetworkResponse.Failure -> {
                    Log.w(TAG, "⚠️ API Failure: ${response.error}")
                    if (localBranches.isEmpty()) emit(emptyList())
                }
                is NetworkResponse.Loading -> {
                    Log.d(TAG, "⏳ API Loading...")
                }
            }
        }
    }.catch { e ->
        Log.e(TAG, "🚨 Error crítico en Repository: ${e.message}", e)
        emit(emptyList())
    }

    override fun getBranchById(id: String): Flow<Branch?> = flow<Branch?> {
        val localEntity = localDataSource.getBranchById(id).first()
        
        if (localEntity != null) {
            emit(localEntity.toDomain())
            if (isCacheFresh(localEntity.lastUpdated)) {
                Log.d(TAG, "✅ Cache fresh para sucursal $id")
                return@flow
            }
        }

        remoteProvider.getBranchById(id).collect { response ->
            if (response is NetworkResponse.Success) {
                response.data?.let { remoteBranch ->
                    try {
                        localDataSource.insertBranch(remoteBranch.toEntity())
                        emit(remoteBranch)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error persistiendo branch: ${e.message}")
                        emit(remoteBranch)
                    }
                }
            } else if (response is NetworkResponse.Failure && localEntity == null) {
                emit(null)
            }
        }
    }.catch { e ->
        Log.e(TAG, "🚨 Error crítico getBranchById: ${e.message}")
        emit(null)
    }

}
