package com.example.ratonean2_app.navigation.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.ratonean2_app.map.presentation.state.MapIntent
import com.example.ratonean2_app.map.presentation.state.MapViewState
import kotlinx.coroutines.launch

@Composable
fun SearchBarHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onClearClick: () -> Unit,
    drawerState: DrawerState,
    onIntent: (MapIntent) -> Unit,
    uiState: MapViewState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                if (query.isEmpty()) {
                    Text(
                        text = "¿Qué buscamos hoy?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = {
                        onQueryChange(it)
                        onIntent(MapIntent.SearchQuery(it))
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
