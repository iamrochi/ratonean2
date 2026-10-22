package com.example.ratonean2_app.core.ui

import androidx.compose.ui.graphics.Color

/**
 * P0.6: parseColor sin crash. El color viene de Room/usuario (string legacy).
 * Ante cualquier valor inválido, devuelve [fallback].
 */
fun safeColor(colorString: String?, fallback: Color = Color(0xFF6200EE)): Color {
    if (colorString.isNullOrBlank()) return fallback
    return runCatching { Color(android.graphics.Color.parseColor(colorString)) }
        .getOrDefault(fallback)
}
