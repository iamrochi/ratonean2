package com.example.ratonean2_app.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ratonean2_app.core.data.database.CatalogDatabase
import com.example.ratonean2_app.core.logging.AppLogger
import com.example.ratonean2_app.product.data.local.ProductLocalDataSource
import org.koin.core.context.GlobalContext

private const val TAG = "CacheMaintenance"

/**
 * Evicción del caché fuera del hot path (antes prune en cada insert = DELETE
 * masivo por página en IO). Corre 1 vez/día con batería ok.
 * Sin esto, el catálogo nacional (GBs) llena el teléfono en una semana.
 */
class CacheMaintenanceWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return runCatching {
            val koin = GlobalContext.getOrNull() ?: return Result.failure()
            val db: CatalogDatabase = koin.get()
            val productSource = ProductLocalDataSource(db.productDao())
            productSource.pruneForMaintenance()
            db.placeSearchCacheDao().pruneOlderThan(
                System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            )
            db.pendingWritesDao().pruneOlderThan(
                System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
            )
            AppLogger.d(TAG, "Cache maintenance OK")
            Result.success()
        }.getOrElse {
            AppLogger.e(TAG, "Cache maintenance falló: ${it.message}", it)
            Result.retry()
        }
    }
}
