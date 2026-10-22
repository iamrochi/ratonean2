package com.example.ratonean2_app.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ratonean2_app.core.data.database.CatalogDatabase
import com.example.ratonean2_app.core.logging.AppLogger
import com.example.ratonean2_app.core.network.ApiUrls
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.koin.core.context.GlobalContext

private const val TAG = "OutboxSync"

/**
 * Reintenta escrituras encoladas sin red con su Idempotency-Key ORIGINAL
 * (no genera una nueva: el backend deduplica y no duplica el recurso).
 * Backoff exponencial de WorkManager; 4xx se descartan (no van a succeeds),
 * 5xx/429/excepciones reintentan.
 */
class OutboxSyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val koin = GlobalContext.getOrNull() ?: return Result.failure()
        val db: CatalogDatabase = koin.get()
        val client: HttpClient = koin.get()
        val dao = db.pendingWritesDao()
        val pending = runCatching { dao.oldest(50) }.getOrDefault(emptyList())
        if (pending.isEmpty()) return Result.success()

        var retried = false
        for (w in pending) {
            // Si superó 5 intentos, se deja para prune semanal (no bloquear la cola).
            if (w.retryCount >= 5) {
                runCatching { dao.remove(w.id) }
                continue
            }
            val ok = runCatching {
                val resp = client.request(ApiUrls.BASE_URL + w.path) {
                    method = HttpMethod.parse(w.method)
                    header("Idempotency-Key", w.idempotencyKey)
                    if (w.bodyJson != null) {
                        contentType(ContentType.Application.Json)
                        setBody(w.bodyJson)
                    }
                }
                when {
                    resp.status.isSuccess() -> {
                        dao.remove(w.id)
                        true
                    }
                    resp.status.value in 400..499 -> {
                        AppLogger.w(TAG, "Outbox ${w.method} ${w.path} 4xx, descartando")
                        dao.remove(w.id)
                        true
                    }
                    else -> {
                        dao.bumpRetry(w.id)
                        retried = true
                        false
                    }
                }
            }.getOrDefault(false)
            if (!ok) retried = true
        }
        return if (retried) Result.retry() else Result.success()
    }
}
