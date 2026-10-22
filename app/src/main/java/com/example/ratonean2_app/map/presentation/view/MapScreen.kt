package com.example.ratonean2_app.map.presentation.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ratonean2_app.map.presentation.component.MapLibreView
import com.example.ratonean2_app.map.presentation.state.LocationPermissionState
import com.example.ratonean2_app.map.presentation.state.MapIntent
import com.example.ratonean2_app.map.presentation.state.MapStatus
import com.example.ratonean2_app.map.presentation.state.MapViewState
import com.example.ratonean2_app.navigation.presentation.components.CenteredText
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    uiState: MapViewState,
    onIntent: (MapIntent) -> Unit,
    onMarkerClick: (branchKey: String, branchName: String) -> Unit = { _, _ -> }
) {
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    val context = LocalContext.current
    fun openAppSettings() {
        val intent = android.content.Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            android.net.Uri.fromParts("package", context.packageName, null)
        ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // P0.6: re-emitir cuando cambia el status del diálogo del sistema.
    // Antes solo se emitía una vez al montar (LaunchedEffect(Unit)).
    LaunchedEffect(locationPermissionState.status.isGranted) {
        onIntent(MapIntent.OnPermissionResult(locationPermissionState.status.isGranted))
    }

    val commerceNames = uiState.branches
        .map { it.commerceId to it.name }
        .distinctBy { it.first }
        .sortedBy { it.second }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.location != null && uiState.mapStatus is MapStatus.Success) {
            MapLibreView(
                lat = uiState.location.latitude,
                lon = uiState.location.longitude,
                branches = uiState.branches,
                onMarkerClick = onMarkerClick,
                activeFilterCommerceIds = uiState.activeFilterCommerceIds
            )

            if (commerceNames.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (uiState.activeFilterCommerceIds.isNotEmpty()) {
                        FilterChip(
                            selected = false,
                            onClick = { onIntent(MapIntent.ClearCommerceFilters) },
                            label = { Text("Todos", style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    commerceNames.forEach { (commerceId, name) ->
                        val isSelected = commerceId in uiState.activeFilterCommerceIds
                        FilterChip(
                            selected = isSelected,
                            onClick = { onIntent(MapIntent.ToggleCommerceFilter(commerceId)) },
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        } else {
            when (uiState.permissionState) {
                LocationPermissionState.NotAsked -> {
                    LaunchedEffect(Unit) {
                        locationPermissionState.launchPermissionRequest()
                    }
                    CenteredText("Solicitando ubicación...")
                }

                else -> {
                    when (val status = uiState.mapStatus) {
                        is MapStatus.Loading -> CenteredText("Buscando tu zona...")
                        is MapStatus.Error -> CenteredText(
                            "Error: ${status.message}",
                            retryLabel = "Reintentar",
                            onRetry = { onIntent(MapIntent.OnPermissionResult(locationPermissionState.status.isGranted)) }
                        )
                        is MapStatus.PermissionDenied -> {
                            // P1.2: denegado permanente → deep-link a Ajustes.
                            // Denegado simple → reintentar diálogo + usar buscador.
                            val permanentlyDenied =
                                (locationPermissionState.status as? PermissionStatus.Denied)
                                    ?.shouldShowRationale == false
                            if (permanentlyDenied) {
                                CenteredText(
                                    "Bloqueaste el GPS. Activalo en Ajustes o usá el buscador.",
                                    retryLabel = "Abrir ajustes",
                                    onRetry = ::openAppSettings
                                )
                            } else {
                                CenteredText(
                                    "Sin permiso de GPS. Por favor, usá el buscador para elegir tu zona.",
                                    retryLabel = "Permitir",
                                    onRetry = { locationPermissionState.launchPermissionRequest() }
                                )
                            }
                        }
                        // P0.6: antes else->{} dejaba pantalla negra con GPS apagado.
                        is MapStatus.LocationDisabled -> {
                            CenteredText(
                                "El GPS está apagado. Activalo o usá el buscador para elegir tu zona.",
                                retryLabel = "Reintentar",
                                onRetry = { onIntent(MapIntent.OnPermissionResult(true)) }
                            )
                        }
                        is MapStatus.Success -> CenteredText("Buscando tu zona...")
                    }
                }
            }
        }
    }
}