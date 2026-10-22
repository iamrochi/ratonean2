package com.example.ratonean2_app.shoppinglist.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ratonean2_app.core.ui.safeColor
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingList

private val ListColors = listOf(
    null,
    "#2563EB",
    "#DC2626",
    "#16A34A",
    "#F59E0B",
    "#9333EA",
    "#EC4899",
    "#06B6D4",
    "#F97316"
)

@Composable
fun ListSelectorContent(
    lists: List<ShoppingList>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onCreate: () -> Unit,
    onDelete: (String) -> Unit,
    onSetBudget: (String, Double?) -> Unit,
    onSetColor: (String, String?) -> Unit,
    onCompare: () -> Unit
) {
    var showBudgetDialog by remember { mutableStateOf<String?>(null) }
    var showColorPicker by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tus Listas",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (lists.size >= 2) {
                TextButton(
                    onClick = onCompare,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Comparar")
                }
            }
        }

        LazyColumn {
            items(lists) { list ->
                val isSelected = list.id == selectedId
                // P0.6: safeColor, antes parseColor crasheaba con strings legacy.
                val listColor = list.color?.let { safeColor(it) }

                ListItem(
                    modifier = Modifier.clickable { onSelect(list.id) },
                    headlineContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = list.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (list.color != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(listColor ?: MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    },
                    supportingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${list.itemCount} productos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (list.itemCount > 0) {
                                Text(
                                    text = " | ${list.checkedCount} checks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            list.budget?.let { budget ->
                                Text(
                                    text = " | Presupuesto: $${String.format("%.0f", budget)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    leadingContent = {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { showColorPicker = list.id }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Palette, contentDescription = "Color", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { showBudgetDialog = list.id }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.AttachMoney, contentDescription = "Presupuesto", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (lists.size > 1) {
                                IconButton(onClick = { onDelete(list.id) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                                }
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

    if (showBudgetDialog != null) {
        val listId = showBudgetDialog!!
        var budgetText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBudgetDialog = null },
            title = { Text("Presupuesto") },
            text = {
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Monto máximo ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("$ ") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val budget = budgetText.toDoubleOrNull()
                    onSetBudget(listId, budget)
                    showBudgetDialog = null
                }) { Text("GUARDAR") }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = null }) { Text("CANCELAR") }
            }
        )
    }

    if (showColorPicker != null) {
        val listId = showColorPicker!!
        AlertDialog(
            onDismissRequest = { showColorPicker = null },
            title = { Text("Color de lista") },
            text = {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ListColors) { color ->
                        val bgColor = color?.let { safeColor(it) } ?: MaterialTheme.colorScheme.surface
                        val isSelected = lists.find { it.id == listId }?.color == color
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                                )
                                .clickable {
                                    onSetColor(listId, color)
                                    showColorPicker = null
                                }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPicker = null }) { Text("CERRAR") }
            }
        )
    }
}
