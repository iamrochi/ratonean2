package com.example.ratonean2_app.product.data.repository

import android.util.Log
import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.product.data.entity.toDomain
import com.example.ratonean2_app.product.data.entity.toEntity
import com.example.ratonean2_app.product.data.local.ProductLocalDataSource
import com.example.ratonean2_app.product.domain.model.Product
import com.example.ratonean2_app.product.domain.provider.ProductProvider
import com.example.ratonean2_app.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class ProductRepositoryImpl(
    private val localDataSource: ProductLocalDataSource,
    private val productProvider: ProductProvider
) : ProductRepository {

    private val TAG = "ProductRepository"
    private val CACHE_EXPIRATION_MS = 10 * 60 * 1000 // 10 minutos

    private fun isCacheFresh(lastUpdated: Long?): Boolean {
        if (lastUpdated == null) return false
        return (System.currentTimeMillis() - lastUpdated) < CACHE_EXPIRATION_MS
    }

    override fun getProductsByBranch(
        branchId: String,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> = flow {
        val localEntities = localDataSource.getProductsByBranch(branchId).first()
        val local = localEntities.map { it.toDomain() }

        if (local.isNotEmpty()) {
            emit(local)
            val mostRecent = localEntities.maxOfOrNull { it.lastUpdated } ?: 0L
            if (isCacheFresh(mostRecent)) {
                Log.d(TAG, "✅ Cache fresh para branch $branchId, saltando API")
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
                emit(emptyList())
            }
        }
    }.catch { emit(emptyList()) }

    override fun getProductsBySearchInBranches(
        branchIds: List<String>,
        query: String,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> = flow {
        // Primero intentamos local (con query filtrada en Room)
        val localEntities = localDataSource.searchProductsInBranches(branchIds, query).first()
        if (localEntities.isNotEmpty()) {
            emit(localEntities.map { it.toDomain() })
            val mostRecent = localEntities.maxOfOrNull { it.lastUpdated } ?: 0L
            if (isCacheFresh(mostRecent)) {
                Log.d(TAG, "✅ Cache fresh para búsqueda '$query', saltando API")
                return@flow
            }
        }

        productProvider.getProductsBySearchInBranches(branchIds, query, limit, offset).collect { response ->
            if (response is NetworkResponse.Success) {
                val remote = response.data.orEmpty()
                localDataSource.insertProducts(remote.map { it.toEntity() })
                emit(remote)
            } else if (response is NetworkResponse.Failure && localEntities.isEmpty()) {
                emit(emptyList())
            }
        }
    }.catch { emit(emptyList()) }

    override fun getPopularProducts(
        branchIds: List<String>,
        limit: Int?,
        offset: Int
    ): Flow<List<Product>> = flow {
        val limitCount = limit ?: 20
        val localEntities = localDataSource.getPopularProductsInBranches(branchIds, limitCount).first()

        if (localEntities.isNotEmpty()) {
            emit(localEntities.map { it.toDomain() })
            val mostRecent = localEntities.maxOfOrNull { it.lastUpdated } ?: 0L
            if (isCacheFresh(mostRecent)) return@flow
        }

        productProvider.getPopularProducts(branchIds, limit, offset).collect { response ->
            if (response is NetworkResponse.Success) {
                val remote = response.data.orEmpty()
                localDataSource.insertProducts(remote.map { it.toEntity() })
                emit(remote)
            }
        }
    }.catch { emit(emptyList()) }

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
        Log.e(TAG, "Error getProductById: ${e.message}")
        emit(null)
    }

    override fun getProductsWithPromos(
        branchId: String,
        brand: String?,
        limit: Int,
        offset: Int
    ): Flow<List<Product>> = flow {
        val localEntities = localDataSource.getPromosByBranch(branchId).first()
        if (localEntities.isNotEmpty()) {
            emit(localEntities.map { it.toDomain() })
            val mostRecent = localEntities.maxOfOrNull { it.lastUpdated } ?: 0L
            if (isCacheFresh(mostRecent)) return@flow
        }

        productProvider.getProductsWithPromos(branchId, brand, limit, offset).collect { response ->
            if (response is NetworkResponse.Success) {
                val remote = response.data.orEmpty()
                localDataSource.insertProducts(remote.map { it.toEntity() })
                emit(remote)
            }
        }
    }.catch { emit(emptyList()) }
}
