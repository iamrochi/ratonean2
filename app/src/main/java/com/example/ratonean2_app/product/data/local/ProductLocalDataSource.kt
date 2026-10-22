package com.example.ratonean2_app.product.data.local

import com.example.ratonean2_app.product.data.dao.ProductDao
import com.example.ratonean2_app.product.data.dao.toFtsQuery
import com.example.ratonean2_app.product.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductLocalDataSource(
    private val productDao: ProductDao
) {
    /** Caché de catálogo: 7 días o 100k filas (lo que ocurra primero). */
    companion object {
        const val MAX_CACHE_AGE_MS = 7L * 24 * 60 * 60 * 1000
        const val MAX_CACHE_ROWS = 100_000
        /** SQLite limita variables a 999 — chunk para IN grandes. */
        const val SQLITE_IN_CHUNK = 400
    }

    fun getProductsByBranch(branchId: String, limit: Int, offset: Int): Flow<List<ProductEntity>> =
        productDao.getByBranch(branchId, limit, offset)

    fun getProductsByBranchKey(branchKey: String, limit: Int, offset: Int): Flow<List<ProductEntity>> =
        productDao.getByBranchKey(branchKey, limit, offset)

    fun getProductById(productId: String): Flow<ProductEntity?> =
        productDao.getById(productId)

    suspend fun getProductsByEan(ean: String, limit: Int = 50): List<ProductEntity> =
        productDao.getByEan(ean, limit)

    fun searchProductsInBranches(
        branchIds: List<String>,
        query: String,
        limit: Int,
        offset: Int
    ): Flow<List<ProductEntity>> =
        // Chunk evita crash con 500+ sucursales en radio denso (CABA/GBA).
        // Trae top por chunk y mezcla en memoria (suficiente para caché local).
        if (branchIds.size <= SQLITE_IN_CHUNK) {
            productDao.searchInBranches(branchIds, query.toFtsQuery(), limit, offset)
        } else {
            combineChunked(branchIds) { chunk ->
                productDao.searchInBranches(chunk, query.toFtsQuery(), limit, offset)
            }
        }

    fun searchProductsInBranchKeys(
        branchKeys: List<String>,
        query: String,
        limit: Int,
        offset: Int
    ): Flow<List<ProductEntity>> =
        if (branchKeys.size <= SQLITE_IN_CHUNK) {
            productDao.searchInBranchKeys(branchKeys, query.toFtsQuery(), limit, offset)
        } else {
            combineChunked(branchKeys) { chunk ->
                productDao.searchInBranchKeys(chunk, query.toFtsQuery(), limit, offset)
            }
        }

    fun getPopularProductsInBranches(
        branchIds: List<String>,
        limit: Int,
        offset: Int = 0
    ): Flow<List<ProductEntity>> =
        if (branchIds.size <= SQLITE_IN_CHUNK) {
            productDao.getPopularInBranches(branchIds, limit, offset)
        } else {
            combineChunked(branchIds) { chunk ->
                productDao.getPopularInBranches(chunk, limit, offset)
            }
        }

    fun getPromosByBranch(branchId: String, limit: Int, offset: Int): Flow<List<ProductEntity>> =
        productDao.getPromosByBranch(branchId, limit, offset)

    suspend fun maxUpdatedByBranch(branchId: String): Long? =
        productDao.maxUpdatedByBranch(branchId)

    suspend fun maxUpdatedByBranchKey(branchKey: String): Long? =
        productDao.maxUpdatedByBranchKey(branchKey)

    suspend fun maxUpdatedInBranchKeys(branchKeys: List<String>): Long? {
        if (branchKeys.isEmpty()) return null
        var best: Long? = null
        for (chunk in branchKeys.chunked(SQLITE_IN_CHUNK)) {
            val v = productDao.maxUpdatedInBranchKeys(chunk)
            if (v != null && (best == null || v > best)) best = v
        }
        return best
    }

    suspend fun maxUpdatedInBranches(branchIds: List<String>): Long? {
        if (branchIds.isEmpty()) return null
        var best: Long? = null
        for (chunk in branchIds.chunked(SQLITE_IN_CHUNK)) {
            val v = productDao.maxUpdatedInBranches(chunk)
            if (v != null && (best == null || v > best)) best = v
        }
        return best
    }

    suspend fun countByBranch(branchId: String): Int =
        productDao.countByBranch(branchId)

    suspend fun insertProducts(products: List<ProductEntity>) {
        if (products.isEmpty()) return
        productDao.insertAll(products)
        // Hot path: sin prune acá (era un DELETE masivo por página en IO).
        // La evicción la hace CacheMaintenanceWorker nocturno.
    }

    /** Llamar desde Worker (no en hot path): evicción por edad + tope filas. */
    suspend fun pruneForMaintenance(now: Long = System.currentTimeMillis()) {
        productDao.pruneOlderThan(now - MAX_CACHE_AGE_MS)
        productDao.enforceRowCap(MAX_CACHE_ROWS)
    }

    suspend fun clearCache() {
        productDao.clear()
    }

    private fun combineChunked(
        branchIds: List<String>,
        query: (List<String>) -> kotlinx.coroutines.flow.Flow<List<ProductEntity>>
    ): kotlinx.coroutines.flow.Flow<List<ProductEntity>> {
        val flows = branchIds.chunked(SQLITE_IN_CHUNK).map { query(it) }
        return kotlinx.coroutines.flow.combine(flows) { arrays ->
            arrays.flatMap { it.asIterable() }
                .distinctBy { it.productKey }
                .sortedBy { it.description }
        }
    }
}
