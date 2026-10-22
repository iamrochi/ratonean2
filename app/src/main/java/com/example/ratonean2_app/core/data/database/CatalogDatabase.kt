package com.example.ratonean2_app.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ratonean2_app.branch.data.dao.BranchDao
import com.example.ratonean2_app.branch.data.entity.BranchEntity
import com.example.ratonean2_app.commerce.data.dao.CommerceDao
import com.example.ratonean2_app.commerce.data.entity.CommerceEntity
import com.example.ratonean2_app.core.data.local.PendingWriteEntity
import com.example.ratonean2_app.core.data.local.PendingWritesDao
import com.example.ratonean2_app.places.data.local.PlaceSearchCacheDao
import com.example.ratonean2_app.places.data.local.PlaceSearchCacheEntity
import com.example.ratonean2_app.product.data.dao.ProductDao
import com.example.ratonean2_app.product.data.entity.ProductEntity
import com.example.ratonean2_app.product.data.entity.ProductFts

/**
 * Caché del catálogo nacional (millones de productos, GBs).
 * - Vive en noBackupFilesDir: excluida de Auto Backup y cloud-backup
 *   automáticamente (una DB de 5GB rompería el backup y la cuota).
 * - Evictable: TTL + tope de filas en ProductLocalDataSource.
 * - Destructive en upgrade: es caché, se re-descarga (nunca datos de usuario).
 * v2: + places_search_cache (Nominatim, 30d TTL, policy OSM ≤1req/s)
 *     + pending_writes (outbox offline-first, reintenta con Idempotency-Key).
 */
@Database(
    entities = [
        CommerceEntity::class,
        BranchEntity::class,
        ProductEntity::class,
        ProductFts::class,
        PlaceSearchCacheEntity::class,
        PendingWriteEntity::class
    ],
    version = 5
)
abstract class CatalogDatabase : RoomDatabase() {

    abstract fun commerceDao(): CommerceDao
    abstract fun branchDao(): BranchDao
    abstract fun productDao(): ProductDao
    abstract fun placeSearchCacheDao(): PlaceSearchCacheDao
    abstract fun pendingWritesDao(): PendingWritesDao
}
