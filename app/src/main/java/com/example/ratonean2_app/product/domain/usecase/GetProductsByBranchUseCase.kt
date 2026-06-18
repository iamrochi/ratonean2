package com.example.ratonean2_app.product.domain.usecase

import com.example.ratonean2_app.product.domain.repository.ProductRepository

class GetProductsByBranchUseCase(private val repository: ProductRepository) {
    operator fun invoke(branchId: String, limit: Int = 50, offset: Int = 0) =
        repository.getProductsByBranch(branchId, limit, offset)
}
