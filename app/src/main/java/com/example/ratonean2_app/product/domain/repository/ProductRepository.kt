package com.example.ratonean2_app.product.domain.repository

import com.example.ratonean2_app.product.data.dto.BranchWithProductsResponse
import com.example.ratonean2_app.product.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductsNearby(
        latitude: Double,
        longitude: Double,
        maxDistance: Double? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<BranchWithProductsResponse>>

    fun getProductsByBranch(
        branchId: String,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<Product>>
    fun getProductsBySearchInBranches(
        branchIds: List<String>,
        query: String,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<Product>>
    fun getPopularProducts(
        branchIds: List<String>,
        limit: Int?,
        offset: Int = 0
    ): Flow<List<Product>>
    fun getProductById(id: String): Flow<Product?>
    fun getProductsWithPromos(
        branchId: String,
        brand: String?,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<Product>>
}
