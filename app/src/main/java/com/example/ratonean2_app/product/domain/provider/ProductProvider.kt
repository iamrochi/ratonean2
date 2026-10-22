package com.example.ratonean2_app.product.domain.provider

import com.example.ratonean2_app.core.network.DEFAULT_PAGE_SIZE

import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.product.data.dto.BranchWithProductsResponse
import com.example.ratonean2_app.product.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductProvider {
    fun getAllProducts(limit: Int = DEFAULT_PAGE_SIZE, offset: Int = 0): Flow<NetworkResponse<List<Product>>>
    fun getProductById(id: String): Flow<NetworkResponse<Product>>
    fun createProduct(product: Product): Flow<NetworkResponse<Unit>>
    fun updateProduct(product: Product): Flow<NetworkResponse<Unit>>
    fun deleteProduct(id: String): Flow<NetworkResponse<Unit>>
    fun getProductsByBranch(
        branchId: String,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<NetworkResponse<List<Product>>>

    fun getProductsByBranchKey(
        branchKey: String,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<NetworkResponse<List<Product>>>
    fun getProductsWithPromos(
        branchId: String,
        brand: String? = null,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<NetworkResponse<List<Product>>>
    fun getProductsBySearchInBranches(
        branchIds: List<String>,
        query: String,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<NetworkResponse<List<Product>>>

    fun getProductsBySearchInBranchKeys(
        branchKeys: List<String>,
        query: String,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<NetworkResponse<List<Product>>>
    fun getPopularProducts(
        branchIds: List<String>,
        limit: Int? = null,
        offset: Int = 0
    ): Flow<NetworkResponse<List<Product>>>

    fun getProductsNearby(
        latitude: Double,
        longitude: Double,
        maxDistance: Double? = null,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<NetworkResponse<List<BranchWithProductsResponse>>>
}
