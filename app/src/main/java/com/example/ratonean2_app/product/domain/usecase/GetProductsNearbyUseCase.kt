package com.example.ratonean2_app.product.domain.usecase

import com.example.ratonean2_app.product.domain.repository.ProductRepository

class GetProductsNearbyUseCase(private val repository: ProductRepository) {
    operator fun invoke(
        latitude: Double,
        longitude: Double,
        maxDistance: Double? = null,
        limit: Int = 50,
        offset: Int = 0
    ) = repository.getProductsNearby(latitude, longitude, maxDistance, limit, offset)
}

