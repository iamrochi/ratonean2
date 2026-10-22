package com.example.ratonean2_app.places.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Una fila por búsqueda normalizada. Evictable (30d), nunca datos de usuario.
 */
@Entity(tableName = "places_search_cache")
data class PlaceSearchCacheEntity(
    @PrimaryKey val query: String,
    val responseJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)
