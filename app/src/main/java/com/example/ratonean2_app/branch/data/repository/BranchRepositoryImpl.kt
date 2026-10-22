package com.example.ratonean2_app.branch.data.repository

import com.example.ratonean2_app.branch.data.entity.toDomain
import com.example.ratonean2_app.branch.data.local.BranchLocalDataSource
import com.example.ratonean2_app.branch.domain.model.Branch
import com.example.ratonean2_app.branch.domain.model.toEntity
import com.example.ratonean2_app.branch.domain.provider.BranchProvider
import com.example.ratonean2_app.branch.domain.repository.BranchRepository
import com.example.ratonean2_app.core.logging.AppLogger
import com.example.ratonean2_app.core.network.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.cancellation.CancellationException

class BranchRepositoryImpl(
    private val localDataSource: BranchLocalDataSource,
    private val remoteProvider: BranchProvider
) : BranchRepository {

    private val TAG = "BranchRepository"
    // Sucursales cambian poco: 7 días SWR para mínimo impacto API.
    // El caché se emite siempre; la red solo refresca si venció.
    private val CACHE_EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000

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
        // P1.5: sin PII (lat/lon) en logcat release — AppLogger no escribe en release.
        AppLogger.d(TAG, "Buscando sucursales cercanas (radio=${distance}km)")

        val localEntities = localDataSource.getNearbyBranches(lat, lon, distance).first()
        val localBranches = localEntities.map { it.toDomain() }

        if (localBranches.isNotEmpty()) {
            AppLogger.d(TAG, "Room: despachando ${localBranches.size} sucursales")
            emit(localBranches)
            
            val mostRecent = localEntities.maxOfOrNull { it.lastUpdated } ?: 0L
            // Si la caché vieja tiene una sola cadena (bug de PKs anteriores),
            // no la damos por buena: forzamos API para traer Coto/Disco/etc.
            val distinctCommerces = localBranches.map { it.commerceId }.distinct().size
            if (isCacheFresh(mostRecent) && distinctCommerces > 1) {
                AppLogger.d(TAG, "Cache fresh, saltando API")
                return@flow
            }
        } else {
            AppLogger.d(TAG, "Room: sin sucursales en este radio")
        }

        AppLogger.d(TAG, "API: consultando sucursales cercanas...")
        remoteProvider.getNearbyBranches(lat, lon, distance, limit, offset).collect { response ->
            when (response) {
                is NetworkResponse.Success -> {
                    val remoteBranches = response.data.orEmpty()
                    AppLogger.d(TAG, "API Success: recibidas ${remoteBranches.size} sucursales")
                    if (remoteBranches.isNotEmpty()) {
                        try {
                            localDataSource.insertBranches(remoteBranches.map { it.toEntity() })
                            emit(remoteBranches)
                        } catch (e: Exception) {
                            AppLogger.e(TAG, "Error al persistir API: ${e.message}")
                            emit(remoteBranches)
                        }
                    }
                }
                is NetworkResponse.Failure -> {
                    AppLogger.w(TAG, "API Failure: ${response.error}")
                    if (localBranches.isEmpty()) emit(emptyList())
                }
                is NetworkResponse.Loading -> {
                    AppLogger.d(TAG, "API Loading...")
                }
            }
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        AppLogger.e(TAG, "Error crítico en Repository: ${e.message}", e)
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    override fun getBranchById(id: String): Flow<Branch?> = flow<Branch?> {
        val localEntity = localDataSource.getBranchById(id).first()
        
        if (localEntity != null) {
            emit(localEntity.toDomain())
            if (isCacheFresh(localEntity.lastUpdated)) {
                AppLogger.d(TAG, "Cache fresh para sucursal")
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
                        AppLogger.e(TAG, "Error persistiendo branch: ${e.message}")
                        emit(remoteBranch)
                    }
                }
            } else if (response is NetworkResponse.Failure && localEntity == null) {
                emit(null)
            }
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        AppLogger.e(TAG, "Error crítico getBranchById: ${e.message}", e)
        emit(null)
    }.flowOn(Dispatchers.IO)

}
