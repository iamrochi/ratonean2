package com.example.ratonean2_app.map.presentation.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ratonean2_app.R
import com.example.ratonean2_app.branch.domain.model.branchKey
import com.example.ratonean2_app.branch.domain.helper.assignImageToCommerce
import com.example.ratonean2_app.branch.domain.model.Branch
import com.example.ratonean2_app.core.logging.AppLogger
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.plugins.annotation.CircleManager
import org.maplibre.android.plugins.annotation.CircleOptions
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "MapLibre"
private const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

/** LRU simple para logos (antes cache infinito sin recycle). */
private class BitmapLru(private val maxSize: Int = 40) :
    LinkedHashMap<String, Bitmap>(maxSize, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
        val evict = size > maxSize
        if (evict) runCatching { eldest?.value?.recycle() }
        return evict
    }
}

private data class RenderSnapshot(
    val ids: Set<String> = emptySet(),
    val filter: Set<String> = emptySet(),
    val userAt: LatLng? = null
)

private sealed interface MapLoadState {
    data object Loading : MapLoadState
    data object Ready : MapLoadState
    data class Error(val message: String) : MapLoadState
}

private fun drawableToBitmap(drawable: Drawable, sizePx: Int = 96): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return Bitmap.createScaledBitmap(drawable.bitmap, sizePx, sizePx, true)
    }
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, sizePx, sizePx)
    drawable.draw(canvas)
    return bitmap
}

@Composable
fun MapLibreView(
    lat: Double,
    lon: Double,
    branches: List<Branch>,
    onMarkerClick: (branchKey: String, branchName: String) -> Unit = { _, _ -> },
    activeFilterCommerceIds: Set<String> = emptySet(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val onMarkerClickState = rememberUpdatedState(onMarkerClick)

    var loadState by remember { mutableStateOf<MapLoadState>(MapLoadState.Loading) }
    var styleAttempt by remember { mutableIntStateOf(0) }

    // Referencias creadas UNA vez (antes se recreaban en cada recomposición).
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val mapRef = remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }
    val symbolManagerRef = remember { mutableStateOf<SymbolManager?>(null) }
    val circleManagerRef = remember { mutableStateOf<CircleManager?>(null) }
    val styleReady = remember { mutableStateOf(false) }
    val addedImages = remember { mutableSetOf<String>() }
    val bitmapCache = remember { BitmapLru() }
    val snapshot = remember { mutableStateOf(RenderSnapshot()) }
    val cameraInit = remember { mutableStateOf(false) }

    // Lifecycle completo del MapView (antes faltaban saveState/lowMemory y
    // se capturaba el context del compose en vez del de la vista).
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            val v = mapViewRef.value ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_START -> v.onStart()
                Lifecycle.Event.ON_RESUME -> v.onResume()
                Lifecycle.Event.ON_PAUSE -> v.onPause()
                Lifecycle.Event.ON_STOP -> v.onStop()
                Lifecycle.Event.ON_DESTROY -> { /* se destruye abajo, una sola vez */ }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            runCatching { symbolManagerRef.value?.onDestroy() }
            runCatching { circleManagerRef.value?.onDestroy() }
            symbolManagerRef.value = null
            circleManagerRef.value = null
            val v = mapViewRef.value
            mapViewRef.value = null
            runCatching {
                v?.onPause()
                v?.onStop()
                v?.onDestroy()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).also { mapViewRef.value = it }
            },
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "Mapa con ${branches.size} sucursales cercanas"
                }
        )

        // Carga de estilo UNA vez por intento (antes en cada recomposición).
        LaunchedEffect(styleAttempt) {
            val view = mapViewRef.value ?: return@LaunchedEffect
            loadState = MapLoadState.Loading
            // Limpia managers del intento anterior (evita duplicados en Reintentar).
            runCatching { symbolManagerRef.value?.onDestroy() }
            runCatching { circleManagerRef.value?.onDestroy() }
            symbolManagerRef.value = null
            circleManagerRef.value = null
            try {
                view.getMapAsync { map ->
                    mapRef.value = map
                    view.addOnDidFailLoadingMapListener { errorMessage ->
                        AppLogger.e(TAG, "Style failed: $errorMessage")
                        loadState = MapLoadState.Error("No se pudo cargar el mapa. Revisá tu conexión.")
                    }
                    map.setStyle(STYLE_URL) { style ->
                        AppLogger.d(TAG, "Style loaded, setting up managers")
                        val sm = SymbolManager(view, map, style).apply {
                            iconAllowOverlap = true
                            iconIgnorePlacement = true
                        }
                        val cm = CircleManager(view, map, style)
                        symbolManagerRef.value = sm
                        circleManagerRef.value = cm

                        sm.addClickListener { symbol ->
                            val data = symbol.data as? com.google.gson.JsonObject
                            if (data != null && data.has("branchKey")) {
                                val branchKey = data.get("branchKey").asString
                                val branchName =
                                    if (data.has("branchName")) data.get("branchName").asString else ""
                                onMarkerClickState.value(branchKey, branchName)
                            }
                            true
                        }

                        styleReady.value = true
                        loadState = MapLoadState.Ready
                    }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                AppLogger.e(TAG, "getMapAsync error: ${e.message}", e)
                loadState = MapLoadState.Error("No se pudo cargar el mapa. Revisá tu conexión.")
            }
        }

        // Cámara: solo al fijar ubicación (no en cada recomposición).
        LaunchedEffect(lat, lon, styleReady.value) {
            val map = mapRef.value
            if (styleReady.value && map != null && !cameraInit.value) {
                cameraInit.value = true
                runCatching {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 13.0),
                        1500
                    )
                }
            }
        }

        // Markers con diff (antes deleteAll()+create en cada cambio = flicker).
        LaunchedEffect(branches, activeFilterCommerceIds, lat, lon, styleReady.value) {
            if (!styleReady.value) return@LaunchedEffect
            val sm = symbolManagerRef.value ?: return@LaunchedEffect
            val cm = circleManagerRef.value ?: return@LaunchedEffect
            val style = mapRef.value?.style ?: return@LaunchedEffect
            try {
                renderMarkersDiff(
                    context, sm, cm, style,
                    branches, activeFilterCommerceIds, LatLng(lat, lon),
                    addedImages, bitmapCache, snapshot
                )
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                AppLogger.e(TAG, "render markers error: ${e.message}", e)
            }
        }

        when (val s = loadState) {
            is MapLoadState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            is MapLoadState.Error -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(s.message, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.size(12.dp))
                Button(onClick = {
                    styleReady.value = false
                    cameraInit.value = false
                    styleAttempt++
                }) { Text("Reintentar") }
            }
            is MapLoadState.Ready -> Unit
        }
    }
}

/**
 * Render con diff:
 * - misma lista + mismo filtro + misma ubicación → no hace nada.
 * - solo agregados (paginación) → crea el delta, sin borrar.
 * - lista reemplazada / filtro / ubicación distintos → rebuild completo.
 */
private fun renderMarkersDiff(
    context: Context,
    symbolManager: SymbolManager,
    circleManager: CircleManager,
    style: org.maplibre.android.maps.Style,
    branches: List<Branch>,
    activeFilterCommerceIds: Set<String>,
    userAt: LatLng,
    addedImages: MutableSet<String>,
    bitmapCache: BitmapLru,
    snapshot: MutableState<RenderSnapshot>
) {
    val prev = snapshot.value
    val filtered = if (activeFilterCommerceIds.isNotEmpty()) {
        branches.filter { it.commerceId in activeFilterCommerceIds }
    } else branches
    val newIds = filtered.map { it.branchKey }.toSet()
    val userMoved = prev.userAt?.let {
        kotlin.math.abs(it.latitude - userAt.latitude) > 1e-4 ||
            kotlin.math.abs(it.longitude - userAt.longitude) > 1e-4
    } ?: true

    if (newIds == prev.ids && activeFilterCommerceIds == prev.filter && !userMoved) return

    val appendedOnly = newIds.containsAll(prev.ids) &&
        activeFilterCommerceIds == prev.filter && !userMoved

    if (!appendedOnly) {
        symbolManager.deleteAll()
        circleManager.deleteAll()
    }

    val toCreate = if (appendedOnly) filtered.filter { it.branchKey !in prev.ids } else filtered

    val userKey = "user-marker"
    if (userKey !in addedImages) {
        AppCompatResources.getDrawable(context, R.drawable.markers_ratonean2)?.let { drawable ->
            val bmp = drawableToBitmap(drawable, 256)
            runCatching {
                style.addImage(userKey, bmp)
                addedImages.add(userKey)
            }
        }
    }
    if (!appendedOnly || userMoved) {
        symbolManager.create(
            SymbolOptions()
                .withLatLng(userAt)
                .withIconImage(userKey)
                .withIconSize(1.0f)
        )
    }

    var branchCount = 0
    for (branch in toCreate) {
        val imageKey = "comm-${branch.commerceId}"
        if (imageKey !in addedImages) {
            val bitmap = bitmapCache[branch.commerceId]
                ?: loadCommerceBitmap(context, branch.commerceId, 256)
            if (bitmap != null) {
                bitmapCache[branch.commerceId] = bitmap
                runCatching {
                    style.addImage(imageKey, bitmap)
                    addedImages.add(imageKey)
                }.onFailure {
                    AppLogger.w(TAG, "addImage failed $imageKey: ${it.message}")
                }
            }
        }

        val finalKey = if (imageKey in addedImages) imageKey else null
        if (finalKey != null) {
            val data = com.google.gson.JsonObject().apply {
                addProperty("branchKey", branch.branchKey)
                addProperty("branchName", branch.name)
            }
            symbolManager.create(
                SymbolOptions()
                    .withLatLng(LatLng(branch.latitude, branch.longitude))
                    .withIconImage(finalKey)
                    .withIconSize(0.3f)
                    .withData(data)
            )
        } else {
            circleManager.create(
                CircleOptions()
                    .withLatLng(LatLng(branch.latitude, branch.longitude))
                    .withCircleRadius(8f)
                    .withCircleColor("#FF2563EB")
                    .withCircleStrokeWidth(2f)
                    .withCircleStrokeColor("#FF1D4ED8")
            )
        }
        branchCount++
    }

    snapshot.value = RenderSnapshot(
        ids = if (appendedOnly) prev.ids + toCreate.map { it.branchKey }.toSet() else newIds,
        filter = activeFilterCommerceIds,
        userAt = userAt
    )
    AppLogger.d(TAG, "Markers: +$branchCount nuevos (filtro=${activeFilterCommerceIds.size})")
}

private fun loadCommerceBitmap(context: Context, commerceId: String, sizePx: Int): Bitmap? {
    return try {
        val resId = assignImageToCommerce(commerceId)
        val drawable = AppCompatResources.getDrawable(context, resId)
        drawable?.let { drawableToBitmap(it, sizePx) }
    } catch (e: CancellationException) { throw e } catch (e: Exception) {
        AppLogger.e(TAG, "loadCommerceBitmap error for $commerceId: ${e.message}")
        null
    }
}
