package com.example.ratonean2_app.map.presentation.state


import androidx.compose.runtime.Immutable
import com.example.ratonean2_app.branch.domain.model.Branch
import com.example.ratonean2_app.map.domain.model.LocationModel
import com.example.ratonean2_app.places.domain.model.PlaceResult
import com.example.ratonean2_app.product.domain.model.Product

@Immutable
data class MapViewState(
    val location: LocationModel? = null,
    val branches: List<Branch> = emptyList(),
    val popularProductsCache: List<Product> = emptyList(),
    val filteredBranches: List<Branch> = emptyList(),
    val searchProducts: List<Product> = emptyList(),
    val searchPlaces: List<PlaceResult> = emptyList(),
    val mapStatus: MapStatus = MapStatus.Loading,
    val searchStatus: SearchStatus = SearchStatus.Idle,
    val permissionState: LocationPermissionState = LocationPermissionState.NotAsked
)

@Immutable
data class ProductList(val items: List<Product>)

sealed interface MapStatus {
    object Loading : MapStatus
    object Success : MapStatus
    data class Error(val message: String) : MapStatus
    object PermissionDenied : MapStatus
    object LocationDisabled : MapStatus
}

sealed class MapUiState {
    object Loading : MapUiState()
    data class Success(
        val location: LocationModel,
        val branches: List<Branch> = emptyList()
    ) : MapUiState()
    data class Error(val message: String) : MapUiState()
    object PermissionDenied : MapUiState()
    object LocationDisabled : MapUiState()
}


sealed class MapIntent {
    object LoadLocation : MapIntent()
    data class UpdateLocation(val lat: Double, val lon: Double, val name: String? = null) : MapIntent()
    data class SearchQuery(val query: String) : MapIntent()
    data class OnPermissionResult(val granted: Boolean) : MapIntent()
    data class AddProductToCart(val product: Product) : MapIntent()
}

sealed interface LocationPermissionState {
    object NotAsked : LocationPermissionState
    object Denied : LocationPermissionState
    object Granted : LocationPermissionState
}