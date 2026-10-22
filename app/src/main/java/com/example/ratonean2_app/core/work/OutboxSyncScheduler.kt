package com.example.ratonean2_app.core.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object OutboxSyncScheduler {
    private const val ONE_SHOT = "outbox-sync-now"
    private const val PERIODIC_CACHE = "cache-maintenance-daily"
    private const val PERIODIC_OUTBOX = "outbox-sync-periodic"

    fun scheduleNow(context: Context) {
        val req = OneTimeWorkRequestBuilder<OutboxSyncWorker>()
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        runCatching {
            WorkManager.getInstance(context).enqueueUniqueWork(ONE_SHOT, ExistingWorkPolicy.APPEND_OR_REPLACE, req)
        }
    }

    /** Llamado desde providers sin Context: usa el appContext guardado al arrancar. */
    fun scheduleNow(): Boolean {
        val ctx = AppContextHolder.appContext ?: return false
        scheduleNow(ctx)
        return true
    }

    fun schedulePeriodic(context: Context) {
        val cacheReq = PeriodicWorkRequestBuilder<CacheMaintenanceWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints(
                    requiredNetworkType = NetworkType.NOT_REQUIRED,
                    requiresBatteryNotLow = true
                )
            )
            .build()
        val outboxReq = PeriodicWorkRequestBuilder<OutboxSyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()
        runCatching {
            val wm = WorkManager.getInstance(context)
            wm.enqueueUniquePeriodicWork(PERIODIC_CACHE, ExistingPeriodicWorkPolicy.KEEP, cacheReq)
            wm.enqueueUniquePeriodicWork(PERIODIC_OUTBOX, ExistingPeriodicWorkPolicy.KEEP, outboxReq)
        }
    }
}
