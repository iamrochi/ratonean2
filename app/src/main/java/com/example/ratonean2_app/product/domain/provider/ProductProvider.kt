package com.example.ratonean2_app.product.domain.provider

import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.product.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductProvider {
    fun getAllProducts(limit: Int = 50, offset: Int = 0): Flow<NetworkResponse<List<Product>>>
    fun getProductById(id: String): Flow<NetworkResponse<Product>>
    fun createProduct(product: Product): Flow<NetworkResponse<Unit>>
    fun updateProduct(product: Product): Flow<NetworkResponse<Unit>>
    fun deleteProduct(id: String): Flow<NetworkResponse<Unit>>
    fun getProductsByBranch(
        branchId: String,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<NetworkResponse<List<Product>>>
    fun getProductsWithPromos(
        branchId: String,
        brand: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<NetworkResponse<List<Product>>>
    fun getProductsBySearchInBranches(
        branchIds: List<String>,
        query: String,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<NetworkResponse<List<Product>>>
    fun getPopularProducts(
        branchId: List<String>,
        limit: Int? = null,
        offset: Int = 0
    ): Flow<NetworkResponse<List<Product>>>
}
