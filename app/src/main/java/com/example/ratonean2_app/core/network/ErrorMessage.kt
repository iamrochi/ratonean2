package com.example.ratonean2_app.core.network

fun String?.toUserFriendlyMessage(): String {
    if (this.isNullOrBlank()) return "Ocurrió un error inesperado"

    val lower = this.lowercase()

    return when {
        lower.startsWith("rate_limited:") || lower.contains("429") || lower.contains("too many") || lower.contains("demasiadas") ->
            "Demasiadas solicitudes. Esperá un momento e intentá de nuevo."
        lower.contains("timeout") || lower.contains("timed out") ->
            "La conexión tardó demasiado. Intentá de nuevo."
        lower.contains("connection refused") || lower.contains("connect") ->
            "No se pudo conectar al servidor. Verificá tu conexión a internet."
        lower.contains("unresolved host") || lower.contains("unknown host") ->
            "No se encontró el servidor. Verificá tu conexión a internet."
        lower.contains("network") || lower.contains("internet") ->
            "Error de red. Verificá tu conexión a internet."
        lower.contains("401") || lower.contains("unauthorized") ->
            "Tu sesión expiró. Iniciá sesión nuevamente."
        lower.contains("403") || lower.contains("forbidden") ->
            "No tenés permiso para realizar esta acción."
        lower.contains("404") || lower.contains("not found") ->
            "El recurso solicitado no fue encontrado."
        lower.contains("500") || lower.contains("internal server") ->
            "El servidor tuvo un problema. Intentá de nuevo en unos minutos."
        lower.contains("502") || lower.contains("503") || lower.contains("504") ->
            "El servicio no está disponible temporalmente. Intentá de nuevo en unos minutos."
        lower.contains("canceled") || lower.contains("cancelled") ->
            "La operación fue cancelada."
        // P0.5: no fugar HTML/JSON/body crudo al usuario.
        else -> "Ocurrió un error inesperado. Intentá de nuevo."
    }
}
