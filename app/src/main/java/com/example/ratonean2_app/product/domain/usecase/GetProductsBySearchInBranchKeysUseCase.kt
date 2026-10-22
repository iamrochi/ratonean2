package com.example.ratonean2_app.product.domain.usecase

import com.example.ratonean2_app.core.network.DEFAULT_PAGE_SIZE
import com.example.ratonean2_app.product.domain.repository.ProductRepository

class GetProductsBySearchInBranchKeysUseCase(private val repository: ProductRepository) {
    operator fun invoke(
        branchKeys: List<String>,
        query: String,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ) = repository.getProductsBySearchInBranchKeys(branchKeys, query, limit, offset)
}
