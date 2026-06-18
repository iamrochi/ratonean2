package com.example.ratonean2_app.navigation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ratonean2_app.navigation.presentation.Screen


@Composable
fun DrawerContent(onDestinationClicked: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface) // Usamos surface para que destaque
            .padding(24.dp)
            .statusBarsPadding()
    ) {
        Text(
            text = "Ratonean2",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

        NavigationDrawerItem(
            label = { Text("Inicio") },
            selected = true,
            onClick = { onDestinationClicked(Screen.Home.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") }
        )
        NavigationDrawerItem(
            label = { Text("Mi Perfil") },
            selected = false,
            onClick = { onDestinationClicked(Screen.Profile.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Mi Perfil") }
        )
        NavigationDrawerItem(
            label = { Text("Lista de Compras") },
            selected = false,
            onClick = { onDestinationClicked(Screen.ShoppingList.route) },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Lista de Compras") }
        )
    }
}

@Composable
fun DrawerItem(label: String, route: String, onClick: (String) -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(route) } // Mandamos la ruta real (ej: "shopping_list")
            .padding(vertical = 12.dp, horizontal = 8.dp),
        style = MaterialTheme.typography.titleMedium
    )
}
