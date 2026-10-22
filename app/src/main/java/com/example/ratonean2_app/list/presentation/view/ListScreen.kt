package com.example.ratonean2_app.list.presentation.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ratonean2_app.branch.domain.model.Branch
import com.example.ratonean2_app.list.presentation.components.BranchItem
import com.example.ratonean2_app.list.presentation.components.CurrentLocationHeader
import com.example.ratonean2_app.list.presentation.components.KeywordRow
import com.example.ratonean2_app.list.presentation.components.ListWithoutLocation
import com.example.ratonean2_app.list.presentation.components.ProductItem
import com.example.ratonean2_app.map.presentation.state.MapIntent
import com.example.ratonean2_app.map.presentation.state.MapViewState
import com.example.ratonean2_app.map.presentation.state.SearchStatus
import com.example.ratonean2_app.product.domain.model.Product


@Composable
fun ListScreen(
    uiState: MapViewState,
    onIntent: (MapIntent) -> Unit,
    onBranchClick: (Branch) -> Unit = {},
    onShoppingListClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        when (val status = uiState.searchStatus) {
            is SearchStatus.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is SearchStatus.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No pudimos cargar los resultados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = status.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // P1.1: retry real. Antes era texto muerto sin acción.
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { onIntent(MapIntent.LoadLocation) }) {
                        Text("Reintentar")
                    }
                }
            }
            else -> {
                if (uiState.location == null && uiState.branches.isEmpty()) {
                    Column {
                        CurrentLocationHeader("Ubicación actual")
                        ListWithoutLocation(onFindLocation = { onIntent(MapIntent.LoadLocation) })
                    }
                } else if (uiState.searchStatus is SearchStatus.Empty) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No encontramos resultados",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Probá con otra búsqueda o explorá las sucursales",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    ListContent(
                        locationName = uiState.location?.name ?: "Ubicación actual",
                        products = uiState.searchProducts.ifEmpty { uiState.popularProductsCache },
                        branches = uiState.filteredBranches.ifEmpty { uiState.branches },
                        hasMoreBranches = uiState.hasMoreBranches,
                        isLoadingMoreBranches = uiState.isLoadingMoreBranches,
                        onIntent = onIntent,
                        onBranchClick = onBranchClick
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onShoppingListClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Mi Lista"
            )
        }
    }
}

@Composable
private fun ListContent(
    locationName: String,
    products: List<Product>,
    branches: List<Branch>,
    hasMoreBranches: Boolean,
    isLoadingMoreBranches: Boolean,
    onIntent: (MapIntent) -> Unit,
    onBranchClick: (Branch) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        CurrentLocationHeader(locationName)

        if (products.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (products.size < 10) "Resultados" else "Productos populares",
                style = MaterialTheme.typography.titleMedium
            )

            val chunkedProducts = products.chunked(2)

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chunkedProducts) { columnProducts ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        columnProducts.forEach { product ->
                            ProductItem(
                                product = product,
                                onAddClick = { onIntent(MapIntent.AddProductToCart(product)) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        KeywordRow(
            keywords = listOf("pan", "leche", "arroz", "yerba", "fideos", "azúcar", "carne", "cerveza", "vino", "lavandina", "jabón", "detergente")
        ) { keyword ->
            onIntent(MapIntent.SearchQuery(keyword))
        }

        if (branches.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sucursales cercanas",
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(branches) { branch ->
                    BranchItem(
                        branch = branch,
                        onClick = { onBranchClick(branch) }
                    )
                }
            }
            // Paginación honesta: el backend pagina de a 20, la UI lo dice y lo pide.
            if (isLoadingMoreBranches) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                }
            } else if (hasMoreBranches) {
                OutlinedButton(
                    onClick = { onIntent(MapIntent.LoadMoreBranches) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Cargar más sucursales (${branches.size} visibles)")
                }
            }
        }
    }
}
