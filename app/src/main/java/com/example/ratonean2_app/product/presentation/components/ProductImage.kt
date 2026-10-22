package com.example.ratonean2_app.product.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import kotlin.math.absoluteValue

private val placeholderBackgrounds = listOf(
    Color(0xFFDBEAFE),
    Color(0xFFDCFCE7),
    Color(0xFFFEF3C7),
    Color(0xFFFCE7F3),
    Color(0xFFEDE9FE),
    Color(0xFFE0F2FE),
)

private val placeholderForegrounds = listOf(
    Color(0xFF1E3A8A),
    Color(0xFF166534),
    Color(0xFF92400E),
    Color(0xFF9D174D),
    Color(0xFF5B21B6),
    Color(0xFF075985),
)

/**
 * Imagen de producto con fallback visual determinístico.
 *
 * SEPA no trae imágenes: si no hay foto real, mostramos un placeholder
 * con la inicial del producto y un color estable. Así todos los productos
 * tienen una imagen en la UI, incluso los que no están en Open Food Facts.
 */
@Composable
fun ProductImage(
    imageUrl: String?,
    description: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val model = imageUrl?.takeIf { it.isNotBlank() }
    if (model == null) {
        ProductImagePlaceholder(description, modifier)
        return
    }

    SubcomposeAsyncImage(
        model = model,
        contentDescription = description,
        contentScale = contentScale,
        modifier = modifier,
        loading = { ProductImagePlaceholder(description, Modifier.fillMaxSize()) },
        error = { ProductImagePlaceholder(description, Modifier.fillMaxSize()) },
    )
}

@Composable
fun ProductImagePlaceholder(
    description: String,
    modifier: Modifier = Modifier,
) {
    val index = (description.hashCode().absoluteValue) % placeholderBackgrounds.size
    val initial = description.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(placeholderBackgrounds[index]),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = placeholderForegrounds[index],
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
