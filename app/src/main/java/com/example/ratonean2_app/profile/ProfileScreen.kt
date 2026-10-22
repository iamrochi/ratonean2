package com.example.ratonean2_app.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ratonean2_app.auth.domain.AuthManager
import com.example.ratonean2_app.profile.presentation.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userId = AuthManager.userId
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        userId?.let { viewModel.loadUser(it) }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            snackbarHostState.showSnackbar("Sesión cerrada")
            viewModel.consumeLogout()
        }
    }

    // Prefs locales: sobreviven rotación (antes se perdían con remember pelado).
    var darkMode by rememberSaveable { mutableStateOf(false) }
    var searchRadius by rememberSaveable { mutableFloatStateOf(5f) }
    var preferredOrder by rememberSaveable { mutableStateOf("Precio más bajo") }

    val categories = listOf("Lácteos", "Verdulería", "Panadería", "Bebidas", "Snacks")
    val selectedCategories = remember { mutableStateListOf<String>() }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editingField by remember { mutableStateOf<String?>(null) } // "name" | "email" | null

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tu perfil", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading && uiState.user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileInfoRow(
                    label = "Nombre",
                    value = uiState.user?.name ?: "Sin datos",
                    buttonText = "Editar",
                    onClick = { editingField = "name" }
                )
                ProfileInfoRow(
                    label = "Correo electrónico",
                    value = uiState.user?.email ?: "Sin datos",
                    buttonText = "Editar",
                    onClick = { editingField = "email" }
                )
                ProfileInfoRow(
                    label = "Contraseña",
                    value = "********",
                    buttonText = "Próximamente",
                    enabled = false,
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Preferencias de búsqueda", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Rango de distancia: ${searchRadius.toInt()} km")
                Slider(
                    value = searchRadius,
                    onValueChange = { searchRadius = it },
                    valueRange = 1f..20f,
                    modifier = Modifier.semantics {
                        contentDescription = "Rango de distancia de búsqueda"
                        stateDescription = "${searchRadius.toInt()} kilómetros"
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Ordenar resultados por:")
                DropdownMenuPreference(
                    options = listOf("Precio más bajo", "Más cercano", "Mejor calidad"),
                    selectedOption = preferredOrder,
                    onOptionSelected = { preferredOrder = it }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Categorías favoritas:")
                FlowRow(
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = category in selectedCategories,
                            onClick = {
                                if (category in selectedCategories)
                                    selectedCategories.remove(category)
                                else
                                    selectedCategories.add(category)
                            },
                            label = { Text(category) },
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .toggleable(
                            value = darkMode,
                            role = Role.Switch,
                            onValueChange = { darkMode = it }
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Modo oscuro")
                    Switch(checked = darkMode, onCheckedChange = null)
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !uiState.isLoading,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar cuenta")
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que querés salir de tu cuenta?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                }) { Text("SALIR") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("CANCELAR") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar cuenta") },
            text = { Text("Esta acción no se puede deshacer. Se borrarán tus datos.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        userId?.let { viewModel.deleteUser(it) }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("ELIMINAR") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("CANCELAR") }
            }
        )
    }

    editingField?.let { field ->
        var text by remember(field) {
            mutableStateOf(
                if (field == "name") uiState.user?.name.orEmpty() else uiState.user?.email.orEmpty()
            )
        }
        val isEmail = field == "email"
        val invalid = if (isEmail) !text.contains("@") || !text.contains(".") else text.isBlank()
        AlertDialog(
            onDismissRequest = { editingField = null },
            title = { Text(if (isEmail) "Editar email" else "Editar nombre") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(if (isEmail) "Email" else "Nombre") },
                    singleLine = true,
                    isError = text.isNotEmpty() && invalid,
                    supportingText = {
                        if (text.isNotEmpty() && invalid) {
                            Text(if (isEmail) "Ingresá un email válido" else "El nombre no puede estar vacío")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = userId
                        val user = uiState.user
                        if (id != null && user != null && !invalid) {
                            viewModel.updateUser(
                                id,
                                if (isEmail) user.name else text.trim(),
                                if (isEmail) text.trim() else user.email
                            )
                            editingField = null
                        }
                    },
                    enabled = !invalid && !uiState.isLoading
                ) { Text("GUARDAR") }
            },
            dismissButton = {
                TextButton(onClick = { editingField = null }) { Text("CANCELAR") }
            }
        )
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    buttonText: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
        TextButton(onClick = onClick, enabled = enabled) {
            Text(buttonText)
        }
    }
}

@Composable
fun DropdownMenuPreference(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedOption)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
