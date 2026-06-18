package com.example.ratonean2_app.shoppinglist.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingList

@Composable
fun ListSelectorContent(
    lists: List<ShoppingList>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onCreate: () -> Unit,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Tus Listas",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
        )

        LazyColumn {
            items(lists) { list ->
                val isSelected = list.id == selectedId

                ListItem(
                    modifier = Modifier.clickable { onSelect(list.id) },
                    headlineContent = {
                        Text(
                            text = list.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        if (lists.size > 1) {
                            IconButton(onClick = { onDelete(list.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else Color.Transparent
                    )
                )
            }

            item {
                TextButton(
                    onClick = onCreate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("CREAR NUEVA LISTA")
                }
            }
        }
    }
}