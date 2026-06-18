package com.example.ratonean2_app.product.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ratonean2_app.R
import com.example.ratonean2_app.ui.theme.RatoneanSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProductCard(
    storeLogoRes: Int,
    productImageUrl: String,
    productName: String,
    productPrice: String,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {}
) {
    var isChecked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .width(200.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = storeLogoRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                IconButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            AsyncImage(
                model = productImageUrl,
                contentDescription = productName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Text(
                text = productName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = productPrice,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Comparar
                Button(
                    onClick = { /* Comparar */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("comparar", fontSize = 11.sp)
                }

                // Botón Agregar (Carrito)
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
                        .weight(1.2f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
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
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isChecked) "¡Listo!" else "Agregar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
