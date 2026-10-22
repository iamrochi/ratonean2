package com.example.ratonean2_app.navigation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ratonean2_app.navigation.presentation.Screen


@Composable
fun DrawerContent(
    currentRoute: String?,
    onDestinationClicked: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
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

        val drawerItems = listOf(
            Triple("Inicio", Icons.Default.Home, Screen.Home.route),
            Triple("Mi Perfil", Icons.Default.Person, Screen.Profile.route),
            Triple("Lista de Compras", Icons.Default.ShoppingCart, Screen.ShoppingList.route),
        )

        drawerItems.forEach { (label, icon, route) ->
            val isSelected = currentRoute == route ||
                    (route == Screen.Home.route && currentRoute?.startsWith("branch_detail") == true)

            NavigationDrawerItem(
                label = { Text(label) },
                selected = isSelected,
                onClick = { onDestinationClicked(route) },
                icon = { Icon(icon, contentDescription = label) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    }
}
