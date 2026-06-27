package com.example.ratonean2_app.product.data.dto

import com.example.ratonean2_app.branch.domain.model.Branch
import com.example.ratonean2_app.product.domain.model.Product
import kotlinx.serialization.Serializable

@Serializable
data class BranchWithProductsResponse(
    val branch: Branch,
    val products: List<Product>
)
