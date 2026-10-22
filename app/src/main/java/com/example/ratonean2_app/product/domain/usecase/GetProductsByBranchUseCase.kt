package com.example.ratonean2_app.product.domain.usecase

import com.example.ratonean2_app.core.network.DEFAULT_PAGE_SIZE

import com.example.ratonean2_app.product.domain.repository.ProductRepository

class GetProductsByBranchUseCase(private val repository: ProductRepository) {
    operator fun invoke(branchId: String, limit: Int = DEFAULT_PAGE_SIZE, offset: Int = 0) =
        repository.getProductsByBranch(branchId, limit, offset)
}
