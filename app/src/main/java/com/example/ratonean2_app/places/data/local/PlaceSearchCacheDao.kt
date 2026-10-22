package com.example.ratonean2_app.places.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Caché de geocodificación Nominatim (OSM).
 * - Policy OSM exige ≤1 req/s + caché: sin esto cada keystroke pega a red
 *   y te banean la IP a escala. 30 días TTL por dirección.
 * - Query normalizada (lowercase+trim) como PK para hits exactos offline.
 * - El JSON guarda la lista completa para no modelar tabla por campo.
 */
@Dao
interface PlaceSearchCacheDao {
    @Query("SELECT responseJson FROM places_search_cache WHERE query = :normalized LIMIT 1")
    suspend fun getResponse(normalized: String): String?

    @Query("SELECT updatedAt FROM places_search_cache WHERE query = :normalized LIMIT 1")
    suspend fun getUpdatedAt(normalized: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: PlaceSearchCacheEntity)

    @Query("DELETE FROM places_search_cache WHERE updatedAt < :minFresh")
    suspend fun pruneOlderThan(minFresh: Long): Int

    @Query("SELECT COUNT(*) FROM places_search_cache")
    suspend fun count(): Int
}
