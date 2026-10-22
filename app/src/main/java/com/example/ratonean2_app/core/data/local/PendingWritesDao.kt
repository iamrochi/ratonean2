package com.example.ratonean2_app.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Outbox para escrituras con mala conexión (offline-first real).
 * POST/PUT/DELETE que fallan por red se guardan con su Idempotency-Key
 * original (la misma que genera NetworkModule) y se reintentan con
 * WorkManager cuando vuelve la red. Sin esto, crear/editar sin señal = dato perdido.
 * Vive en el caché (evictable) porque el backend es la fuente de verdad;
 * la UI marca el item como "pendiente de sincronizar".
 */
@Dao
interface PendingWritesDao {
    @Query("SELECT * FROM pending_writes ORDER BY createdAt ASC LIMIT :limit")
    suspend fun oldest(limit: Int = 50): List<PendingWriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(write: PendingWriteEntity)

    @Query("DELETE FROM pending_writes WHERE id = :id")
    suspend fun remove(id: Long)

    @Query("UPDATE pending_writes SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun bumpRetry(id: Long)

    @Query("SELECT COUNT(*) FROM pending_writes")
    suspend fun count(): Int

    @Query("DELETE FROM pending_writes WHERE createdAt < :olderThan")
    suspend fun pruneOlderThan(olderThan: Long): Int
}
