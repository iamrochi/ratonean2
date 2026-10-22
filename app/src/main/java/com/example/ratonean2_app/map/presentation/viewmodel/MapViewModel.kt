package com.example.ratonean2_app.map.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratonean2_app.branch.domain.model.branchKey
import com.example.ratonean2_app.branch.domain.usecase.GetNearbyBranchesUseCase
import com.example.ratonean2_app.core.logging.AppLogger
import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.core.network.toUserFriendlyMessage
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
import com.example.ratonean2_app.product.domain.usecase.GetProductsBySearchInBranchKeysUseCase
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

/** Página de sucursales/productos nearby. En sync con paginate() default. */
private const val NEARBY_PAGE_SIZE = 20

@Stable
class MapViewModel(
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val getNearbyBranchesUseCase: GetNearbyBranchesUseCase,
    private val getProductsBySearchInBranchKeys: GetProductsBySearchInBranchKeysUseCase,
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
    private var loadMoreJob: Job? = null
    // Racha de páginas sin nada nuevo (caché ya completo): corta el "cargar más".
    private var noFreshStreak = 0

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
            is MapIntent.ToggleCommerceFilter -> handleToggleCommerceFilter(intent.commerceId)
            is MapIntent.ClearCommerceFilters -> handleClearCommerceFilters()
            is MapIntent.LoadMoreBranches -> loadMoreBranches()
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
                if (e is kotlinx.coroutines.CancellationException) throw e
                _state.update { it.copy(mapStatus = MapStatus.Error(e.message.toUserFriendlyMessage())) }
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
                _state.update { it.copy(mapStatus = MapStatus.Error("No pudimos obtener tu ubicación. Activá el GPS e intentá de nuevo."), location = null) }
            }
        }
    }

    private fun handleUpdateLocation(lat: Double, lon: Double, name: String?, distance: Double = 5.0) {
        jobs.updateJob?.cancel()
        loadMoreJob?.cancel()
        // Reinicia paginación: la primera página remota es offset 0..20.
        noFreshStreak = 0
        _state.update {
            it.copy(
                branchesOffset = NEARBY_PAGE_SIZE,
                hasMoreBranches = true,
                isLoadingMoreBranches = false
            )
        }
        jobs.updateJob = viewModelScope.launch {
            try {
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

                launch {
                    try {
                        getNearbyBranchesUseCase(lat, lon, distance, NEARBY_PAGE_SIZE, 0).collect { branches ->
                            _state.update {
                                it.copy(
                                    branches = branches,
                                    mapStatus = MapStatus.Success
                                )
                            }
                        }
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        AppLogger.e("MapViewModel", "Error loading branches: ${e.message}")
                        _state.update {
                            it.copy(mapStatus = MapStatus.Error(e.message.toUserFriendlyMessage()))
                        }
                    }
                }

                try {
                    getProductsNearbyUseCase(lat, lon, distance, NEARBY_PAGE_SIZE, 0).collect { results ->
                        val products = results.flatMap { it.products }.distinctBy { it.productId }

                        _state.update { state ->
                            state.copy(
                                popularProductsCache = products,
                                searchProducts = if (state.searchStatus == SearchStatus.Idle) products else state.searchProducts
                            )
                        }
                    }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    AppLogger.e("MapViewModel", "Error loading products: ${e.message}")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _state.update {
                    it.copy(
                        mapStatus = MapStatus.Error(e.message.toUserFriendlyMessage())
                    )
                }
            }
        }
    }

    private fun loadPopularProducts(branchIds: List<String>) {
        viewModelScope.launch {
            try {
                getPopularProductsUseCase(branchIds, 5).collect { products ->
                    _state.update { it.copy(popularProductsCache = products) }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                AppLogger.e("MapViewModel", "Error loading popular products: ${e.message}")
            }
        }
    }

    private fun handleSearch(query: String) {
        jobs.searchJob?.cancel()

        if (query.isBlank()) {
            _state.update {
                it.copy(
                    searchStatus = SearchStatus.Idle,
                    searchProducts = it.popularProductsCache,
                    filteredBranches = emptyList()
                )
            }
            return
        }

        jobs.searchJob = viewModelScope.launch {
            delay(500)
            _state.update { it.copy(searchStatus = SearchStatus.Loading) }

            try {
                val filteredBranches = _state.value.branches.filter { branch ->
                    branch.name.contains(query, ignoreCase = true)
                }

                val branchKeys = _state.value.branches.map { it.branchKey }

                getProductsBySearchInBranchKeys(branchKeys, query).collect { products ->
                    val finalProducts = products
                    var places: List<PlaceResult> = emptyList()

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
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _state.update {
                    it.copy(searchStatus = SearchStatus.Error(e.message.toUserFriendlyMessage()))
                }
            }
        }
    }

    private suspend fun fetchPlaces(query: String): List<PlaceResult> {
        val response: NetworkResponse<List<PlaceResult>> = try {
            getPlacesUseCase(query)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            AppLogger.e("MapViewModel", "Error en fetchPlaces: ${e.message}")
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

    private fun handleToggleCommerceFilter(commerceId: String) {
        _state.update { current ->
            val currentFilters = current.activeFilterCommerceIds
            val newFilters = if (commerceId in currentFilters) {
                currentFilters - commerceId
            } else {
                currentFilters + commerceId
            }
            current.copy(activeFilterCommerceIds = newFilters)
        }
    }

    private fun handleClearCommerceFilters() {
        _state.update { it.copy(activeFilterCommerceIds = emptySet()) }
    }

    /**
     * Paginación honesta: pide la siguiente página remota (offset) y anexa
     * solo ids nuevos (distinct). El repo emite primero caché local y después
     * red; el distinct hace que el caché no duplique. Termina cuando la página
     * viene corta (< PAGE) o dos páginas seguidas no aportan nada nuevo.
     */
    private fun loadMoreBranches() {
        val loc = _state.value.location ?: return
        if (_state.value.isLoadingMoreBranches || !_state.value.hasMoreBranches) return
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            _state.update { it.copy(isLoadingMoreBranches = true) }
            try {
                val offset = _state.value.branchesOffset
                var added = 0
                var lastPageSize = 0
                getNearbyBranchesUseCase(
                    loc.latitude, loc.longitude, 5.0, NEARBY_PAGE_SIZE, offset
                ).collect { page ->
                    lastPageSize = page.size
                    val known = _state.value.branches.map { it.branchKey }.toSet()
                    val fresh = page.filter { it.branchKey !in known }
                    if (fresh.isNotEmpty()) {
                        added += fresh.size
                        noFreshStreak = 0
                        _state.update { it.copy(branches = it.branches + fresh) }
                    } else {
                        noFreshStreak++
                    }
                    _state.update {
                        it.copy(hasMoreBranches = page.size >= NEARBY_PAGE_SIZE && noFreshStreak < 2)
                    }
                }
                _state.update {
                    it.copy(
                        branchesOffset = offset + NEARBY_PAGE_SIZE,
                        // Si el collect terminó sin aportar nada y la página era corta, es el fin.
                        hasMoreBranches = if (added == 0 && lastPageSize < NEARBY_PAGE_SIZE) false
                        else it.hasMoreBranches
                    )
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                AppLogger.e("MapViewModel", "Error cargando más sucursales: ${e.message}")
                // Honesto: no ofrecer reintento infinito si la red falla.
                _state.update { it.copy(hasMoreBranches = false) }
            } finally {
                _state.update { it.copy(isLoadingMoreBranches = false) }
            }
        }
    }
}
