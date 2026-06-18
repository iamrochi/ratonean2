package com.example.ratonean2_app.shoppinglist.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ratonean2_app.shoppinglist.presentation.components.ListSelectorContent
import com.example.ratonean2_app.shoppinglist.presentation.components.ShoppingItemRow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteListDialog by remember { mutableStateOf(false) }
    var showClearCheckedDialog by remember { mutableStateOf(false) }
    var listToEdit by remember { mutableStateOf<String?>(null) }
    var listNameToEdit by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            state.selectedList?.name ?: "Mi Lista",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (state.items.isNotEmpty()) {
                            Text(
                                "${state.items.size} productos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        state.selectedList?.let {
                            listToEdit = it.id
                            listNameToEdit = it.name
                            showRenameDialog = true
                        }
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Renombrar")
                    }
                    IconButton(onClick = { showSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Cambiar Lista",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = state.items.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total estimado",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$${String.format("%.2f", state.totalPrice)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { 
                                if (state.items.any { it.isChecked }) {
                                    showClearCheckedDialog = true 
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            enabled = state.items.any { it.isChecked }
                        ) {
                            Text("TERMINAR")
                        }
                    }
                }
            }
        }
    ) { padding ->
        // ... previous code ...
        if (showClearCheckedDialog) {
            AlertDialog(
                onDismissRequest = { showClearCheckedDialog = false },
                title = { Text("¿Finalizar compra?") },
                text = { Text("Se eliminarán los productos marcados como comprados de tu lista.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onIntent(ShoppingIntent.ClearCheckedItems)
                            showClearCheckedDialog = false
                        }
                    ) { Text("FINALIZAR") }
                },
                dismissButton = {
                    TextButton(onClick = { showClearCheckedDialog = false }) { Text("CANCELAR") }
                }
            )
        }
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                ListSelectorContent(
                    lists = state.lists,
                    selectedId = state.selectedList?.id,
                    onSelect = {
                        viewModel.onIntent(ShoppingIntent.SelectList(it))
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) showSheet = false
                        }
                    },
                    onCreate = {
                        showCreateDialog = true
                        showSheet = false
                    },
                    onDelete = { id ->
                        listToEdit = id
                        showDeleteListDialog = true
                    }
                )
            }
        }

        // Dialogs
        if (showCreateDialog) {
            var newName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Nueva Lista") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre de la lista") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newName.isNotBlank()) {
                                viewModel.onIntent(ShoppingIntent.CreateList(newName))
                                showCreateDialog = false
                            }
                        }
                    ) { Text("CREAR") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("CANCELAR") }
                }
            )
        }

        if (showRenameDialog) {
            var newName by remember { mutableStateOf(listNameToEdit) }
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Renombrar Lista") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nuevo nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newName.isNotBlank() && listToEdit != null) {
                                viewModel.onIntent(ShoppingIntent.RenameList(listToEdit!!, newName))
                                showRenameDialog = false
                            }
                        }
                    ) { Text("GUARDAR") }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) { Text("CANCELAR") }
                }
            )
        }

        if (showDeleteListDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteListDialog = false },
                title = { Text("¿Eliminar lista?") },
                text = { Text("Esta acción no se puede deshacer y se perderán todos los productos de esta lista.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            listToEdit?.let {
                                viewModel.onIntent(ShoppingIntent.DeleteList(it))
                                showDeleteListDialog = false
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("ELIMINAR") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteListDialog = false }) { Text("CANCELAR") }
                }
            )
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.items.isEmpty()) {
            EmptyListState(modifier = Modifier.padding(padding))
        } else {
            val groupedItems = state.items.groupBy { it.commerceId }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                groupedItems.forEach { (commerceId, items) ->
                    item(key = "header_$commerceId") {
                        CommerceHeader(commerceId = commerceId)
                    }
                    items(
                        items = items,
                        key = { it.id }
                    ) { item ->
                        ShoppingItemRow(
                            item = item,
                            onIntent = viewModel::onIntent,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommerceHeader(commerceId: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Store,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Comercio: $commerceId", // Ideally replace with commerce name
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun EmptyListState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "¡Tu lista está vacía!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Explora productos y agrégalos para empezar a ahorrar hoy mismo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { /* TODO: Navegar a búsqueda o categorías */ },
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("BUSCAR PRODUCTOS")
        }
    }
}
