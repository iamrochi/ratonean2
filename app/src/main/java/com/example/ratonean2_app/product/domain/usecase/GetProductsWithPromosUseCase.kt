package com.example.ratonean2_app.product.domain.usecase

import com.example.ratonean2_app.product.domain.repository.ProductRepository

class GetProductsWithPromosUseCase(private val repository: ProductRepository) {
    operator fun invoke(branchId: String, brand: String? = null, limit: Int = 50, offset: Int = 0) =
        repository.getProductsWithPromos(branchId, brand, limit, offset)
}
