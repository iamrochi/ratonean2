package com.example.ratonean2_app.shoppinglist.presentation.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ratonean2_app.core.ui.safeColor
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingList
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CompareListsScreen(
    onBack: () -> Unit,
    viewModel: CompareListsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Comparar Listas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Seleccioná las dos listas que querés comparar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ListPicker(
                    label = "Lista A",
                    lists = state.lists.filter { it.id != state.listBId },
                    selectedId = state.listAId,
                    onSelect = { viewModel.onIntent(CompareIntent.SelectListA(it)) },
                    modifier = Modifier.weight(1f)
                )
                ListPicker(
                    label = "Lista B",
                    lists = state.lists.filter { it.id != state.listAId },
                    selectedId = state.listBId,
                    onSelect = { viewModel.onIntent(CompareIntent.SelectListB(it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.onIntent(CompareIntent.Compare) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.listAId != null && state.listBId != null && !state.isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CompareArrows, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("COMPARAR")
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.sharedItems.isNotEmpty() || state.uniqueToA.isNotEmpty() || state.uniqueToB.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SummaryCard(state)
                    }

                    if (state.sharedItems.isNotEmpty()) {
                        item {
                            SectionHeader("En común (${state.sharedItems.size})", MaterialTheme.colorScheme.primary)
                        }
                        items(state.sharedItems) { shared ->
                            SharedItemRow(shared)
                        }
                    }

                    if (state.uniqueToA.isNotEmpty()) {
                        item {
                            SectionHeader("Solo en Lista A (${state.uniqueToA.size})", Color(0xFF2563EB))
                        }
                        items(state.uniqueToA) { item ->
                            UniqueItemRow(item.description, item.brand, item.price, item.quantity, Color(0xFF2563EB))
                        }
                    }

                    if (state.uniqueToB.isNotEmpty()) {
                        item {
                            SectionHeader("Solo en Lista B (${state.uniqueToB.size})", Color(0xFFDC2626))
                        }
                        items(state.uniqueToB) { item ->
                            UniqueItemRow(item.description, item.brand, item.price, item.quantity, Color(0xFFDC2626))
                        }
                    }

                    if (state.sharedItems.isEmpty() && state.uniqueToA.isEmpty() && state.uniqueToB.isEmpty()) {
                        item {
                            EmptyCompare()
                        }
                    }
                }
            } else if (state.listAId != null && state.listBId != null) {
                EmptyCompare()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListPicker(
    label: String,
    lists: List<ShoppingList>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedList = lists.find { it.id == selectedId }

    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedList?.name ?: "Seleccionar...",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                lists.forEach { list ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (list.color != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                safeColor(list.color),
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(list.name)
                            }
                        },
                        onClick = {
                            onSelect(list.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(state: CompareUiState) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = state.summary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lista A", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "$${String.format("%.2f", state.priceDiffA)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lista B", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "$${String.format("%.2f", state.priceDiffB)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
                val diff = state.priceDiffB - state.priceDiffA
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Diferencia", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${if (diff >= 0) "+" else ""}$${String.format("%.2f", diff)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (diff < 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun SharedItemRow(shared: SharedItem) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shared.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = shared.brand,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${String.format("%.2f", shared.priceA)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", shared.priceB)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold
                    )
                }
                val diff = shared.difference
                if (diff != 0.0) {
                    Text(
                        text = "${if (diff > 0) "+" else ""}$${String.format("%.2f", diff)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (diff < 0) Color(0xFF16A34A) else Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun UniqueItemRow(description: String, brand: String, price: Double, quantity: Int, color: Color) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(brand, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$${String.format("%.2f", price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                if (quantity > 1) {
                    Text("x$quantity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EmptyCompare() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CompareArrows,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Elegí dos listas y presioná COMPARAR para ver las diferencias",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
