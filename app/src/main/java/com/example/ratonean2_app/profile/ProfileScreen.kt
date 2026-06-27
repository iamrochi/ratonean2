package com.example.ratonean2_app.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ratonean2_app.auth.domain.AuthManager
import com.example.ratonean2_app.profile.presentation.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userId = AuthManager.userId

    LaunchedEffect(userId) {
        userId?.let { viewModel.loadUser(it) }
    }

    var darkMode by remember { mutableStateOf(false) }
    var searchRadius by remember { mutableFloatStateOf(5f) }
    var preferredOrder by remember { mutableStateOf("Precio más bajo") }

    val categories = listOf("Lácteos", "Verdulería", "Panadería", "Bebidas", "Snacks")
    val selectedCategories = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tu perfil", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { /* logout */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = Color.Black
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Icono de perfil centrado
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                        .border(2.dp, Color.Gray, CircleShape)
                        .padding(8.dp),
                    tint = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { /* Editar foto */ }) {
                    Text("Editar foto")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sección: Información personal
                ProfileInfoRow("Nombre", uiState.user?.name ?: "Cargando...", "Editar") {
                    // Acción editar nombre
                }
                ProfileInfoRow("Correo electrónico", uiState.user?.email ?: "Cargando...", "Editar") {
                    // Acción editar email
                }
                ProfileInfoRow("Contraseña", "********", "Cambiar contraseña") {
                    // Acción cambiar pass
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sección: Preferencias de búsqueda
                Text("Preferencias de búsqueda", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Rango de distancia: ${searchRadius.toInt()} km")
                Slider(
                    value = searchRadius,
                    onValueChange = { searchRadius = it },
                    valueRange = 1f..20f
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

                // Sección: Apariencia
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Modo oscuro")
                    Switch(checked = darkMode, onCheckedChange = { darkMode = it })
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de eliminar cuenta
                OutlinedButton(
                    onClick = { userId?.let { viewModel.deleteUser(it) } },
                    border = BorderStroke(1.dp, Color.Red),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Eliminar cuenta")
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String, buttonText: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
        TextButton(onClick = onClick) {
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