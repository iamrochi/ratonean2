package com.example.ratonean2_app.core.logging

import android.util.Log
import com.example.ratonean2_app.BuildConfig

/**
 * Logger único + Crashlytics listo.
 *
 * Estado: si existe `app/google-services.json`, el build aplica los plugins de
 * Firebase y compila las dependencias (ver app/build.gradle.kts `hasGoogleServicesJson`).
 * Sin ese archivo todo sigue compilando y el reporte es no-op.
 *
 * Para activarlo solo tenés que:
 *  1. Descargar `google-services.json` de Firebase Console y ponerlo en `app/`.
 *  2. Rebuildear. Listo: [e] manda log + excepción a Crashlytics en release,
 *     y en debug solo logcat (para no ensuciar el dashboard).
 */
object AppLogger {
    private const val TAG_DEFAULT = "Ratonean"

    fun d(tag: String = TAG_DEFAULT, msg: String) {
        if (BuildConfig.DEBUG) Log.d(tag, msg)
    }

    fun w(tag: String = TAG_DEFAULT, msg: String) {
        if (BuildConfig.DEBUG) Log.w(tag, msg)
    }

    fun e(tag: String = TAG_DEFAULT, msg: String, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg, tr)
        } else {
            // En release sin debugger, igual deja rastro mínimo en logcat del sistema.
            Log.e(tag, msg)
        }
        reportToCrashlytics(tag, msg, tr)
    }

    /**
     * Llama a FirebaseCrashlytics por reflexión para NO exigir la dependencia
     * en compilación. Si Firebase no está inicializado (sin json), es no-op.
     */
    private fun reportToCrashlytics(tag: String, msg: String, tr: Throwable?) {
        if (!BuildConfig.CRASHLYTICS_ENABLED) return
        runCatching {
            val clazz = Class.forName("com.google.firebase.crashlytics.FirebaseCrashlytics")
            val instance = clazz.getMethod("getInstance").invoke(null) ?: return
            clazz.getMethod("log", String::class.java).invoke(instance, "$tag: $msg")
            if (tr != null) {
                clazz.getMethod("recordException", Throwable::class.java).invoke(instance, tr)
            }
        }
    }
}
