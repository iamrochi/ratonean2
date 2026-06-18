package com.example.ratonean2_app.product.data.local

import com.example.ratonean2_app.product.data.dao.ProductDao
import com.example.ratonean2_app.product.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductLocalDataSource(
    private val productDao: ProductDao
) {
    fun getProductsByBranch(branchId: String): Flow<List<ProductEntity>> =
        productDao.getByBranch(branchId)

    fun getProductById(productId: String): Flow<ProductEntity?> =
        productDao.getById(productId)

    fun searchProductsInBranches(branchIds: List<String>, query: String): Flow<List<ProductEntity>> =
        productDao.searchInBranches(branchIds, query)

    fun getPopularProductsInBranches(branchIds: List<String>, limit: Int): Flow<List<ProductEntity>> =
        productDao.getPopularInBranches(branchIds, limit)

    fun getPromosByBranch(branchId: String): Flow<List<ProductEntity>> =
        productDao.getPromosByBranch(branchId)

    suspend fun insertProducts(products: List<ProductEntity>) {
        productDao.insertAll(products)
    }

    suspend fun clearCache() {
        productDao.clear()
    }
}
