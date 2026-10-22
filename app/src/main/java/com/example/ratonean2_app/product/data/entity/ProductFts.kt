package com.example.ratonean2_app.product.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

/**
 * Índice full-text de productos (FTS4). Con 12M+ de productos en catálogo,
 * `LIKE '%q%'` haría full-scan; MATCH usa el índice FTS.
 * `contentEntity` genera triggers que lo sincronizan (incluye el prune).
 * Solo se indexa lo cacheado en el dispositivo, no el catálogo nacional.
 */
@Fts4(contentEntity = ProductEntity::class)
@Entity(tableName = "productsFts")
data class ProductFts(
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "brand") val brand: String
)
