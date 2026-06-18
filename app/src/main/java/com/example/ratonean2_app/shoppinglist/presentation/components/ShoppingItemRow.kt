package com.example.ratonean2_app.shoppinglist.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingItem
import com.example.ratonean2_app.shoppinglist.presentation.ShoppingIntent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onIntent: (ShoppingIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onIntent(ShoppingIntent.DeleteItem(item.id))
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val alignment = Alignment.CenterEnd
            val color = MaterialTheme.colorScheme.errorContainer

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (item.isChecked)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { onIntent(ShoppingIntent.ToggleItem(item.id, it)) }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                        color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", item.price)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Controles de cantidad
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (item.quantity > 1) {
                                    onIntent(ShoppingIntent.UpdateQuantity(item.id, item.quantity - 1))
                                } else {
                                    onIntent(ShoppingIntent.DeleteItem(item.id))
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (item.quantity > 1) Icons.Default.Remove else Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (item.quantity == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "${item.quantity}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = { onIntent(ShoppingIntent.UpdateQuantity(item.id, item.quantity + 1)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
