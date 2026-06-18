package com.example.ratonean2_app.product.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ratonean2_app.product.domain.model.Product
import kotlinx.serialization.Serializable

@Entity(tableName = "products")
@Serializable
data class ProductEntity(
    val commerceId: String,
    val flagId: String,
    val branchId: String,

    @PrimaryKey val productId: String,

    val ean: String,
    val description: String,
    val presentationQuantity: Double,
    val presentationUnit: String,
    val brand: String,
    val listPrice: Double,
    val referencePrice: Double,
    val referenceQuantity: Double,
    val referenceUnit: String,
    val promoPrice1: Double?,
    val promoLegend1: String?,
    val promoPrice2: Double?,
    val promoLegend2: String?,
    val imageUrl: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun ProductEntity.toDomain(): Product = Product(
    commerceId = commerceId,
    flagId = flagId,
    branchId = branchId,
    productId = productId,
    ean = ean,
    description = description,
    presentationQuantity = presentationQuantity,
    presentationUnit = presentationUnit,
    brand = brand,
    listPrice = listPrice,
    referencePrice = referencePrice,
    referenceQuantity = referenceQuantity,
    referenceUnit = referenceUnit,
    promoPrice1 = promoPrice1,
    promoLegend1 = promoLegend1,
    promoPrice2 = promoPrice2,
    promoLegend2 = promoLegend2,
    imageUrl = imageUrl
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    commerceId = commerceId,
    flagId = flagId,
    branchId = branchId,
    productId = productId,
    ean = ean,
    description = description,
    presentationQuantity = presentationQuantity,
    presentationUnit = presentationUnit,
    brand = brand,
    listPrice = listPrice,
    referencePrice = referencePrice,
    referenceQuantity = referenceQuantity,
    referenceUnit = referenceUnit,
    promoPrice1 = promoPrice1,
    promoLegend1 = promoLegend1,
    promoPrice2 = promoPrice2,
    promoLegend2 = promoLegend2,
    imageUrl = imageUrl,
    lastUpdated = System.currentTimeMillis()
)
