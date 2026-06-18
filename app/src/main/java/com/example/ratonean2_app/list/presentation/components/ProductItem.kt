package com.example.ratonean2_app.list.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ratonean2_app.branch.domain.helper.assignImageToCommerce
import com.example.ratonean2_app.product.domain.model.Product
import com.example.ratonean2_app.ui.theme.RatoneanSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProductItem(product: Product, onAddClick: () -> Unit) {
    var isChecked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .height(155.dp)
            .width(170.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                product.imageUrl?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = null,
                        modifier = Modifier.size(45.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                product.commerceId?.let { id ->
                    AsyncImage(
                        model = assignImageToCommerce(id),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = product.description,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                minLines = 2,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "$${product.listPrice}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (!isChecked) {
                        isChecked = true
                        onAddClick()
                        scope.launch {
                            delay(1500)
                            isChecked = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isChecked) RatoneanSuccess else MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isChecked) Icons.Default.Check else Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isChecked) "¡Listo!" else "Sumar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}