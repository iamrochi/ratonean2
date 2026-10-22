package com.example.ratonean2_app.shoppinglist.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ratonean2_app.shoppinglist.presentation.components.ListSelectorContent
import com.example.ratonean2_app.shoppinglist.presentation.components.ShoppingItemRow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onCompare: () -> Unit = {},
    onSearchProducts: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: ShoppingListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSheet by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteListDialog by remember { mutableStateOf(false) }
    var showClearCheckedDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var listToEdit by remember { mutableStateOf<String?>(null) }
    var listNameToEdit by remember { mutableStateOf("") }

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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Ordenar")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Por defecto") },
                                onClick = {
                                    viewModel.onIntent(ShoppingIntent.SetSortMode(ItemSortMode.DEFAULT))
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Nombre A-Z") },
                                onClick = {
                                    viewModel.onIntent(ShoppingIntent.SetSortMode(ItemSortMode.NAME_ASC))
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Nombre Z-A") },
                                onClick = {
                                    viewModel.onIntent(ShoppingIntent.SetSortMode(ItemSortMode.NAME_DESC))
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Precio menor") },
                                onClick = {
                                    viewModel.onIntent(ShoppingIntent.SetSortMode(ItemSortMode.PRICE_ASC))
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Precio mayor") },
                                onClick = {
                                    viewModel.onIntent(ShoppingIntent.SetSortMode(ItemSortMode.PRICE_DESC))
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Por prioridad") },
                                onClick = {
                                    viewModel.onIntent(ShoppingIntent.SetSortMode(ItemSortMode.PRIORITY))
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Comprados al final") },
                                onClick = {
                                    viewModel.onIntent(ShoppingIntent.SetSortMode(ItemSortMode.CHECKED_LAST))
                                    showSortMenu = false
                                }
                            )
                        }
                    }
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
                    },
                    onSetBudget = { id, budget ->
                        viewModel.onIntent(ShoppingIntent.SetBudget(id, budget))
                    },
                    onSetColor = { id, color ->
                        viewModel.onIntent(ShoppingIntent.SetColor(id, color))
                    },
                    onCompare = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showSheet = false
                            onCompare()
                        }
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
                        supportingText = {
                            if (newName.isBlank()) Text("El nombre no puede estar vacío")
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newName.isNotBlank()) {
                                viewModel.onIntent(ShoppingIntent.CreateList(newName.trim()))
                                showCreateDialog = false
                            }
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onIntent(ShoppingIntent.CreateList(newName.trim()))
                            showCreateDialog = false
                        },
                        enabled = newName.isNotBlank()
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
                        supportingText = {
                            if (newName.isBlank()) Text("El nombre no puede estar vacío")
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newName.isNotBlank() && listToEdit != null) {
                                viewModel.onIntent(ShoppingIntent.RenameList(listToEdit!!, newName.trim()))
                                showRenameDialog = false
                            }
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            listToEdit?.let {
                                viewModel.onIntent(ShoppingIntent.RenameList(it, newName.trim()))
                            }
                            showRenameDialog = false
                        },
                        enabled = newName.isNotBlank() && listToEdit != null
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.items.isEmpty()) {
            EmptyListState(modifier = Modifier.padding(padding), onSearch = onSearchProducts)
        } else {
            // Agrupado memoizado: antes se recalculaba en cada recomposición.
            val groupedItems = remember(state.sortedItems) {
                state.sortedItems.groupBy { it.commerceId }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (state.items.isNotEmpty()) {
                    item(key = "progress") {
                        ProgressSection(state)
                    }
                }

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
                // Sin lookup de comercios en este estado: muestra el id tal cual,
                // con fallback honesto si viene vacío.
                text = if (commerceId.isBlank()) "Comercio sin nombre" else "Comercio: $commerceId",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun ProgressSection(state: ShoppingViewState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${state.checkedCount}/${state.items.size} comprados",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${(state.progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription =
                        "${state.checkedCount} de ${state.items.size} productos comprados"
                },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        state.budgetProgress?.let { progress ->
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Presupuesto: $${String.format("%.0f", state.selectedList?.budget ?: 0.0)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "$${String.format("%.2f", state.totalPrice)} / $${String.format("%.0f", state.selectedList?.budget ?: 0.0)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (progress > 1.0f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress.coerceAtMost(1.0f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (progress > 1.0f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun EmptyListState(modifier: Modifier = Modifier, onSearch: () -> Unit = {}) {
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
            // CTA cableado: antes era el único camino de salida del empty y no hacía nada.
            onClick = onSearch,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("BUSCAR PRODUCTOS")
        }
    }
}
