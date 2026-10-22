package com.example.ratonean2_app.shoppinglist.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ratonean2_app.product.presentation.components.ProductImage
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingItem
import com.example.ratonean2_app.shoppinglist.presentation.ShoppingIntent
import kotlinx.coroutines.delay

private val PriorityHigh = Color(0xFFDC2626)
private val PriorityMedium = Color(0xFFF59E0B)
private val PriorityLow = Color(0xFF22C55E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onIntent: (ShoppingIntent) -> Unit,
    isEditing: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showDetails by remember { mutableStateOf(false) }
    var noteText by remember(item.notes) { mutableStateOf(item.notes ?: "") }

    // Guarda notas con debounce: antes un intent por letra (spam a DB).
    LaunchedEffect(noteText) {
        delay(500)
        val saved = item.notes ?: ""
        if (noteText != saved) {
            onIntent(ShoppingIntent.UpdateNotes(item.id, noteText.ifBlank { null }))
        }
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onIntent(ShoppingIntent.DeleteItem(item.id))
                true
            } else false
        }
    )

    val cardColor by animateColorAsState(
        targetValue = if (item.isChecked)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.surface,
        label = "cardColor"
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (item.isChecked) 0.dp else 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.priority != ShoppingItem.PRIORITY_NONE) {
                        val priorityLabel = when (item.priority) {
                            ShoppingItem.PRIORITY_HIGH -> "Prioridad alta"
                            ShoppingItem.PRIORITY_MEDIUM -> "Prioridad media"
                            else -> "Prioridad baja"
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (item.priority) {
                                        ShoppingItem.PRIORITY_HIGH -> PriorityHigh
                                        ShoppingItem.PRIORITY_MEDIUM -> PriorityMedium
                                        ShoppingItem.PRIORITY_LOW -> PriorityLow
                                        else -> Color.Transparent
                                    }
                                )
                                // Solo-color no alcanza (WCAG 1.4.1): anuncia la prioridad.
                                .semantics { contentDescription = "$priorityLabel: ${item.description}" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    ProductImage(
                        imageUrl = item.imageUrl,
                        description = item.description,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { onIntent(ShoppingIntent.ToggleItem(item.id, it)) },
                        modifier = Modifier.semantics {
                            contentDescription = "Marcar ${item.description} como comprado"
                        }
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$${String.format("%.2f", item.price)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            if (item.quantity > 1) {
                                Text(
                                    text = " x${item.quantity} = $${String.format("%.2f", item.subtotal)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (item.quantity > 1) onIntent(ShoppingIntent.UpdateQuantity(item.id, item.quantity - 1))
                                        else onIntent(ShoppingIntent.DeleteItem(item.id))
                                    },
                                    // Target 48dp (antes 32): cumple Material sin achicar el icono.
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        if (item.quantity > 1) Icons.Default.Remove else Icons.Default.Delete,
                                        contentDescription = if (item.quantity > 1)
                                            "Disminuir cantidad de ${item.description}"
                                        else "Quitar ${item.description} de la lista",
                                        modifier = Modifier.size(20.dp),
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
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Aumentar cantidad de ${item.description}",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { showDetails = !showDetails },
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Icon(
                                if (showDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (showDetails) "Ocultar detalles" else "Mostrar detalles",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showDetails,
                    enter = expandVertically(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                        Text("Prioridad", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            PriorityChip("Alta", PriorityHigh, item.priority == ShoppingItem.PRIORITY_HIGH) {
                                onIntent(ShoppingIntent.UpdatePriority(item.id, ShoppingItem.PRIORITY_HIGH))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            PriorityChip("Media", PriorityMedium, item.priority == ShoppingItem.PRIORITY_MEDIUM) {
                                onIntent(ShoppingIntent.UpdatePriority(item.id, ShoppingItem.PRIORITY_MEDIUM))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            PriorityChip("Baja", PriorityLow, item.priority == ShoppingItem.PRIORITY_LOW) {
                                onIntent(ShoppingIntent.UpdatePriority(item.id, ShoppingItem.PRIORITY_LOW))
                            }
                            if (item.priority != ShoppingItem.PRIORITY_NONE) {
                                Spacer(modifier = Modifier.width(8.dp))
                                PriorityChip("Quitar", MaterialTheme.colorScheme.surfaceVariant, false) {
                                    onIntent(ShoppingIntent.UpdatePriority(item.id, ShoppingItem.PRIORITY_NONE))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Notas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text("Notas") },
                            placeholder = { Text("Ej: preferencia marca X, sin TACC...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(80.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityChip(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    // FilterChip M3: ripple + rol + estado seleccionable (antes Surface+clickable mudo).
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.PriorityHigh, contentDescription = null, modifier = Modifier.size(12.dp), tint = color) }
        } else null
    )
}
