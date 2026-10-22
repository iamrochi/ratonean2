package com.example.ratonean2_app.product.domain.repository

import com.example.ratonean2_app.core.network.DEFAULT_PAGE_SIZE

import com.example.ratonean2_app.product.data.dto.BranchWithProductsResponse
import com.example.ratonean2_app.product.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductsNearby(
        latitude: Double,
        longitude: Double,
        maxDistance: Double? = null,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<List<BranchWithProductsResponse>>

    fun getProductsByBranch(
        branchId: String,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<List<Product>>
    fun getProductsByBranchKey(
        branchKey: String,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<List<Product>>
    fun getProductsBySearchInBranches(
        branchIds: List<String>,
        query: String,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<List<Product>>
    fun getProductsBySearchInBranchKeys(
        branchKeys: List<String>,
        query: String,
        limit: Int = DEFAULT_PAGE_SIZE,
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
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<List<Product>>
}
