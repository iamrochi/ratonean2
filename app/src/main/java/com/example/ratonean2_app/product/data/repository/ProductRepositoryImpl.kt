package com.example.ratonean2_app.product.data.repository

import com.example.ratonean2_app.branch.data.local.BranchLocalDataSource
import com.example.ratonean2_app.branch.data.entity.toDomain
import com.example.ratonean2_app.branch.domain.model.toEntity
import com.example.ratonean2_app.core.logging.AppLogger
import com.example.ratonean2_app.core.network.DEFAULT_PAGE_SIZE
import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.product.data.dto.BranchWithProductsResponse
import com.example.ratonean2_app.product.data.entity.toDomain
import com.example.ratonean2_app.product.data.entity.toEntity
import com.example.ratonean2_app.product.data.local.ProductLocalDataSource
import com.example.ratonean2_app.product.domain.model.Product
import com.example.ratonean2_app.product.domain.provider.ProductProvider
import com.example.ratonean2_app.product.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.cancellation.CancellationException

class ProductRepositoryImpl(
    private val localDataSource: ProductLocalDataSource,
    private val branchLocalDataSource: BranchLocalDataSource,
    private val productProvider: ProductProvider
) : ProductRepository {

    private val TAG = "ProductRepository"
    // Offline-first Argentina: 24h SWR. El caché se emite siempre primero;
    // la API solo refresca si venció. Antes 10min = refetch en cada apertura.
    private val CACHE_EXPIRATION_MS = 24 * 60 * 60 * 1000

    private fun isCacheFresh(lastUpdated: Long?): Boolean {
        if (lastUpdated == null) return false
        return (System.currentTimeMillis() - lastUpdated) < CACHE_EXPIRATION_MS
    }

    override fun getProductsNearby(
        latitude: Double,
        longitude: Double,
        maxDistance: Double?,
        limit: Int,
        offset: Int
    ): Flow<List<BranchWithProductsResponse>> = flow {
        // P0-fix offline-first: antes solo logueaba el caché y si la API fallaba
        // emitía vacío. Ahora emite caché primero (SWR) para que el mapa/lista
        // funcionen sin red, y después intenta refrescar.
        // Solo pagina la red (offset>0); el caché se emite en offset==0.
        if (offset == 0) {
            runCatching {
                val cachedBranches = branchLocalDataSource
                    .getNearbyBranches(latitude, longitude, maxDistance ?: 5.0).first()
                if (cachedBranches.isNotEmpty()) {
                    AppLogger.d(TAG, "📦 Room: emitiendo ${cachedBranches.size} sucursales cacheadas (offline-first)")
                    val cached = cachedBranches.take(limit.coerceAtLeast(1)).map { branchEntity ->
                        val products = runCatching {
                            localDataSource.getProductsByBranchKey(
                                branchEntity.branchKey, DEFAULT_PAGE_SIZE, 0
                            ).first()
                        }.getOrDefault(emptyList()).map { it.toDomain() }
                        BranchWithProductsResponse(branchEntity.toDomain(), products)
                    }
                    emit(cached)
                }
            }.onFailure { AppLogger.w(TAG, "Room fallback nearby falló: ${it.message}") }
        }

        productProvider.getProductsNearby(latitude, longitude, maxDistance, limit, offset).collect { response ->
            if (response is NetworkResponse.Success) {
                val data = response.data.orEmpty()

                // Cachear sucursales y productos obtenidos
                val allBranches = data.map { it.branch }
                val allProducts = data.flatMap { it.products }

                if (allBranches.isNotEmpty()) {
                    runCatching {
                        branchLocalDataSource.insertBranches(allBranches.map { it.toEntity() })
                    }.onFailure { AppLogger.e(TAG, "Error cacheando branches: ${it.message}") }
                }
                if (allProducts.isNotEmpty()) {
                    runCatching {
                        localDataSource.insertProducts(allProducts.map { it.toEntity() })
                    }.onFailure { AppLogger.e(TAG, "Error cacheando products: ${it.message}") }
                }
                // En paginación (offset>0) se emite la página remota; en offset==0
                // se re-emite para refrescar lo que ya mostramos desde caché.
                if (data.isNotEmpty() || offset > 0) emit(data)
            } else if (response is NetworkResponse.Failure) {
                AppLogger.w(TAG, "⚠️ getProductsNearby Failure: ${response.error} (se conserva caché emitido)")
            }
        }
    }.catch { e ->
        // P0.5: no tragar en silencio. Log + vacío (la UI ya muestra último estado).
        if (e is CancellationException) throw e
        AppLogger.e(TAG, "Error getProductsNearby: ${e.message}", e)
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    override fun getProductsByBranch(
        branchId: String,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> = flow {
        // Página local acotada (nunca full-table) + frescura por MAX().
        val localPage = localDataSource.getProductsByBranch(branchId, limit, offset).first()
        val local = localPage.map { it.toDomain() }

        if (local.isNotEmpty()) {
            emit(local)
            if (isCacheFresh(localDataSource.maxUpdatedByBranch(branchId))) {
                AppLogger.d(TAG, "Cache fresh para branch $branchId, saltando API")
                return@flow
            }
        }

        productProvider.getProductsByBranch(branchId, limit, offset).collect { response ->
            if (response is NetworkResponse.Success) {
                val remote = response.data.orEmpty()
                if (remote.isNotEmpty()) {
                    localDataSource.insertProducts(remote.map { it.toEntity() })
                    emit(remote)
                }
            } else if (response is NetworkResponse.Failure && local.isEmpty()) {
                AppLogger.w(TAG, "getProductsByBranch Failure: ${response.error}")
                emit(emptyList())
            }
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        AppLogger.e(TAG, "Error getProductsByBranch: ${e.message}", e)
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    override fun getProductsByBranchKey(
        branchKey: String,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> = flow {
        val localPage = localDataSource.getProductsByBranchKey(branchKey, limit, offset).first()
        val local = localPage.map { it.toDomain() }

        if (local.isNotEmpty()) {
            emit(local)
            if (isCacheFresh(localDataSource.maxUpdatedByBranchKey(branchKey))) {
                return@flow
            }
        }

        productProvider.getProductsByBranchKey(branchKey, limit, offset).collect { response ->
            if (response is NetworkResponse.Success) {
                val remote = response.data.orEmpty()
                if (remote.isNotEmpty()) {
                    localDataSource.insertProducts(remote.map { it.toEntity() })
                    emit(remote)
                }
            } else if (response is NetworkResponse.Failure && local.isEmpty()) {
                emit(emptyList())
            }
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        AppLogger.e(TAG, "Error getProductsByBranchKey: ${e.message}", e)
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    override fun getProductsBySearchInBranches(
        branchIds: List<String>,
        query: String,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> = flow {
        // FTS local primero (MATCH indexado, no LIKE full-scan).
        val localPage = localDataSource.searchProductsInBranches(branchIds, query, limit, offset).first()
        if (localPage.isNotEmpty()) {
            emit(localPage.map { it.toDomain() })
            if (isCacheFresh(localDataSource.maxUpdatedInBranches(branchIds))) {
                AppLogger.d(TAG, "Cache fresh para búsqueda, saltando API")
                return@flow
            }
        }

        productProvider.getProductsBySearchInBranches(branchIds, query, limit, offset).collect { response ->
            if (response is NetworkResponse.Success) {
                val remote = response.data.orEmpty()
                localDataSource.insertProducts(remote.map { it.toEntity() })
                emit(remote)
            } else if (response is NetworkResponse.Failure && localPage.isEmpty()) {
                AppLogger.w(TAG, "search Failure: ${response.error}")
                emit(emptyList())
            }
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        AppLogger.e(TAG, "Error search: ${e.message}", e)
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    override fun getProductsBySearchInBranchKeys(
        branchKeys: List<String>,
        query: String,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> = flow {
        val localPage = localDataSource.searchProductsInBranchKeys(branchKeys, query, limit, offset).first()
        if (localPage.isNotEmpty()) {
            emit(localPage.map { it.toDomain() })
            if (isCacheFresh(localDataSource.maxUpdatedInBranchKeys(branchKeys))) {
                return@flow
            }
        }

        productProvider.getProductsBySearchInBranchKeys(branchKeys, query, limit, offset).collect { response ->
            if (response is NetworkResponse.Success) {
                val remote = response.data.orEmpty()
                localDataSource.insertProducts(remote.map { it.toEntity() })
                emit(remote)
            } else if (response is NetworkResponse.Failure && localPage.isEmpty()) {
                emit(emptyList())
            }
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        AppLogger.e(TAG, "Error search branchKeys: ${e.message}", e)
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    override fun getPopularProducts(
        branchIds: List<String>,
        limit: Int?,
        offset: Int
    ): Flow<List<Product>> = flow {
        val limitCount = limit ?: DEFAULT_PAGE_SIZE
        val localPage = localDataSource.getPopularProductsInBranches(branchIds, limitCount, offset).first()

        if (localPage.isNotEmpty()) {
            emit(localPage.map { it.toDomain() })
            if (isCacheFresh(localDataSource.maxUpdatedInBranches(branchIds))) return@flow
        }

        productProvider.getPopularProducts(branchIds, limit, offset).collect { response ->
            if (response is NetworkResponse.Success) {
                val remote = response.data.orEmpty()
                localDataSource.insertProducts(remote.map { it.toEntity() })
                emit(remote)
            } else if (response is NetworkResponse.Failure) {
                AppLogger.w(TAG, "popular Failure: ${response.error}")
            }
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        AppLogger.e(TAG, "Error popular: ${e.message}", e)
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    override fun getProductById(id: String): Flow<Product?> = flow<Product?> {
        val local = localDataSource.getProductById(id).first()

        if (local != null) {
            emit(local.toDomain())
            if (isCacheFresh(local.lastUpdated)) return@flow
        }

        productProvider.getProductById(id).collect { response ->
            if (response is NetworkResponse.Success) {
                response.data?.let { product ->
                    localDataSource.insertProducts(listOf(product.toEntity()))
                    emit(product)
                }
            }
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        AppLogger.e(TAG, "Error getProductById: ${e.message}", e)
        emit(null)
    }.flowOn(Dispatchers.IO)

    override fun getProductsWithPromos(
        branchId: String,
        brand: String?,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> = flow {
        val localPage = localDataSource.getPromosByBranch(branchId, limit, offset).first()
        if (localPage.isNotEmpty()) {
            emit(localPage.map { it.toDomain() })
            if (isCacheFresh(localDataSource.maxUpdatedByBranch(branchId))) return@flow
        }

        productProvider.getProductsWithPromos(branchId, brand, limit, offset).collect { response ->
            if (response is NetworkResponse.Success) {
                val remote = response.data.orEmpty()
                localDataSource.insertProducts(remote.map { it.toEntity() })
                emit(remote)
            } else if (response is NetworkResponse.Failure) {
                AppLogger.w(TAG, "promos Failure: ${response.error}")
            }
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        AppLogger.e(TAG, "Error promos: ${e.message}", e)
        emit(emptyList())
    }.flowOn(Dispatchers.IO)
}
