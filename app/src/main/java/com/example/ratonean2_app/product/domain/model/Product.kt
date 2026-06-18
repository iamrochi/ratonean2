package com.example.ratonean2_app.product.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable


@Serializable
@Immutable
data class Product(
    val commerceId: String,
    val flagId: String,
    val branchId: String,
    val productId: String,
    val ean: String,
    val description: String,
    val presentationQuantity: Double,
    val presentationUnit: String,
    val brand: String,
    val listPrice: Double,
    val referencePrice: Double,
    val referenceQuantity: Double,
    val referenceUnit: String,
    val promoPrice1: Double? = null,
    val promoLegend1: String? = null,
    val promoPrice2: Double? = null,
    val promoLegend2: String? = null,
    val imageUrl: String? = null
)
