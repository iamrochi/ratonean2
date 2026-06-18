package com.example.ratonean2_app.list.presentation.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import kotlin.collections.isNotEmpty


@Composable
fun ListScreen(
    uiState: MapViewState,
    onIntent: (MapIntent) -> Unit,
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
                Text(
                    text = "Error: ${status.message}",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                if (uiState.location == null && uiState.branches.isEmpty()) {
                    Column {
                        CurrentLocationHeader("Tu corazón ♡")
                        ListWithoutLocation()
                    }
                } else {
                    ListContent(
                        locationName = uiState.location?.name ?: "Tu corazón ♡",
                        products = uiState.searchProducts.ifEmpty { uiState.popularProductsCache },
                        branches = uiState.filteredBranches.ifEmpty { uiState.branches },
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}

@Composable
private fun ListContent(
    locationName: String,
    products: List<Product>,
    branches: List<Branch>,
    onIntent: (MapIntent) -> Unit
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
                    BranchItem(branch = branch)
                }
            }
        }
    }
}