package com.example.ratonean2_app.product.domain.usecase

import com.example.ratonean2_app.product.domain.repository.ProductRepository

class GetPopularProductsUseCase(private val repository: ProductRepository) {
    operator fun invoke(branchIds: List<String>, limit: Int? = null, offset: Int = 0) =
        repository.getPopularProducts(branchIds, limit, offset)
}
