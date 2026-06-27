package com.example.ratonean2_app.map.presentation.viewmodel

import android.R.attr.description
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratonean2_app.branch.domain.usecase.GetNearbyBranchesUseCase
import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.map.domain.model.LocationModel
import com.example.ratonean2_app.map.domain.model.LocationResult
import com.example.ratonean2_app.map.domain.usercase.GetUserLocationUseCase
import com.example.ratonean2_app.map.presentation.state.LocationPermissionState
import com.example.ratonean2_app.map.presentation.state.MapIntent
import com.example.ratonean2_app.map.presentation.state.MapStatus
import com.example.ratonean2_app.map.presentation.state.MapViewState
import com.example.ratonean2_app.map.presentation.state.SearchStatus
import com.example.ratonean2_app.places.domain.model.PlaceResult
import com.example.ratonean2_app.places.domain.usecase.GetPlacesUseCase
import com.example.ratonean2_app.product.domain.usecase.GetPopularProductsUseCase
import com.example.ratonean2_app.product.domain.usecase.GetProductsBySearchInBranches
import com.example.ratonean2_app.product.domain.usecase.GetProductsNearbyUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.AddToActiveListUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.ManageShoppingItemUseCase
import com.example.ratonean2_app.user.domain.usecase.GetUserPrefsLocationUseCase
import com.example.ratonean2_app.user.domain.usecase.SaveUserPrefsLocationUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

@Stable
class MapViewModel(
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val getNearbyBranchesUseCase: GetNearbyBranchesUseCase,
    private val getProductsBySearchInBranches: GetProductsBySearchInBranches,
    private val getPopularProductsUseCase: GetPopularProductsUseCase,
    private val getProductsNearbyUseCase: GetProductsNearbyUseCase,
    private val getPlacesUseCase: GetPlacesUseCase,
    private val getUserPrefsLocationUseCase: GetUserPrefsLocationUseCase,
    private val saveUserPrefsLocationUseCase: SaveUserPrefsLocationUseCase,
    private val addToActiveListUseCase: AddToActiveListUseCase,

) : ViewModel() {

    private val _state = MutableStateFlow(MapViewState())
    val state: StateFlow<MapViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    @Immutable
    private class SearchManager {
        var searchJob: Job? = null
        var updateJob: Job? = null
    }

    private val jobs = SearchManager()

    fun onIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.LoadLocation -> handleLoadLocation()

            is MapIntent.OnPermissionResult -> {
                handlePermissionResult(intent.granted)
                handleLoadLocation()
            }
            is MapIntent.UpdateLocation -> handleUpdateLocation(intent.lat, intent.lon, intent.name)
            is MapIntent.SearchQuery -> handleSearch(intent.query)
            is MapIntent.AddProductToCart -> handleAddProduct(intent.product)
        }
    }

    private fun handleLoadLocation() {
        // Carga inicial de productos aunque no haya ubicación aún
        loadPopularProducts(emptyList())

        viewModelScope.launch {
            _state.update { it.copy(mapStatus = MapStatus.Loading) }
            try {
                val savedLocation = getUserPrefsLocationUseCase().first()

                if (savedLocation != null) {
                    handleUpdateLocation(
                        lat = savedLocation.latitude,
                        lon = savedLocation.longitude,
                        name = savedLocation.addressName
                    )
                } else {
                    loadFromGps()
                }
            } catch (e: Exception) {
                _state.update { it.copy(mapStatus = MapStatus.Error(e.message ?: "Error al cargar ubicación")) }
            }
        }
    }

    private suspend fun loadFromGps() {
        when (val result = getUserLocationUseCase()) {
            is LocationResult.Success -> {
                handleUpdateLocation(
                    result.location.latitude,
                    result.location.longitude,
                    result.location.name
                )
            }
            is LocationResult.PermissionDenied -> {
                _state.update { it.copy(mapStatus = MapStatus.PermissionDenied, location = null) }
            }
            is LocationResult.LocationDisabled -> {
                _state.update { it.copy(mapStatus = MapStatus.LocationDisabled, location = null) }
            }
            is LocationResult.Error -> {
                _state.update { it.copy(mapStatus = MapStatus.Error("Error de GPS"), location = null) }
            }
        }
    }

    private fun handleUpdateLocation(lat: Double, lon: Double, name: String?, distance: Double = 5.0) {
        jobs.updateJob?.cancel()
        jobs.updateJob = viewModelScope.launch {

            saveUserPrefsLocationUseCase(
                com.example.ratonean2_app.user.domain.model.UserLocation(
                    latitude = lat,
                    longitude = lon,
                    addressName = name,
                    isGpsBased = false
                )
            )

            val newLocation = LocationModel(name, lat, lon)
            _state.update { it.copy(location = newLocation, mapStatus = MapStatus.Loading) }

            // 1. Mostrar sucursales rápido (desde cache local si existe)
            val branchesJob = launch {
                getNearbyBranchesUseCase(lat, lon, distance).collect { branches ->
                    _state.update { it.copy(
                        branches = branches,
                        mapStatus = MapStatus.Success // Ya podemos mostrar el mapa
                    ) }
                }
            }

            // 2. Cargar productos populares y actualizar todo desde el nuevo endpoint combinado
            getProductsNearbyUseCase(lat, lon, distance).collect { results ->
                branchesJob.cancel() // Ya tenemos data fresca, cancelamos el otro flow
                val branches = results.map { it.branch }
                val products = results.flatMap { it.products }.distinctBy { it.productId }

                _state.update { state ->
                    state.copy(
                        branches = branches,
                        popularProductsCache = products,
                        searchProducts = if (state.searchStatus == SearchStatus.Idle) products else state.searchProducts,
                        mapStatus = MapStatus.Success
                    )
                }
            }
        }
    }

    private fun loadPopularProducts(branchIds: List<String>) {
        viewModelScope.launch {
            getPopularProductsUseCase(branchIds, 5).collect { products ->
                _state.update { it.copy(popularProductsCache = products) }
            }
        }
    }

    private fun handleSearch(query: String) {
        jobs.searchJob?.cancel()

        if (query.isBlank()) {
            _state.update { it.copy(
                searchStatus = SearchStatus.Idle,
                searchProducts = it.popularProductsCache,
                filteredBranches = emptyList()
            ) }
            return
        }

        jobs.searchJob = viewModelScope.launch {
            delay(500)
            _state.update { it.copy(searchStatus = SearchStatus.Loading) }

            val filteredBranches = _state.value.branches.filter { branch ->
                branch.name.contains(query, ignoreCase = true)
            }

            val branchIds = _state.value.branches.map { it.branchId }

            getProductsBySearchInBranches(branchIds, query).collect { products ->
                val finalProducts = products
                var places: List<PlaceResult> = emptyList()

                // Si no hay productos, buscamos lugares (Google Places)
                if (finalProducts.isEmpty()) {
                    places = fetchPlaces(query)
                }

                _state.update {
                    it.copy(
                        filteredBranches = filteredBranches,
                        searchProducts = finalProducts,
                        searchPlaces = places,
                        searchStatus = if (finalProducts.isEmpty() && places.isEmpty() && filteredBranches.isEmpty())
                            SearchStatus.Empty else SearchStatus.Success
                    )
                }
            }
        }
    }

    private suspend fun fetchPlaces(query: String): List<PlaceResult> {
        val response: NetworkResponse<List<PlaceResult>> = try {
            getPlacesUseCase(query)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e("MapViewModel", "Error en fetchPlaces: ${e.message}")
            NetworkResponse.Failure(e.message ?: "Error")
        }

        return when (response) {
            is NetworkResponse.Success -> response.data.orEmpty()
            else -> emptyList()
        }
    }

    private fun handlePermissionResult(granted: Boolean) {
        val pState = if (granted) LocationPermissionState.Granted else LocationPermissionState.Denied
        _state.update { it.copy(permissionState = pState) }
    }

    private fun handleAddProduct(product: com.example.ratonean2_app.product.domain.model.Product) {
        viewModelScope.launch {
            addToActiveListUseCase(product)
            _events.emit("Se agregó ${product.description} a tu lista")
        }
    }
}
