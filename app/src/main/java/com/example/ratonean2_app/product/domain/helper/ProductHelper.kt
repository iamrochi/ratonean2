package com.example.ratonean2_app.product.domain.helper

import io.ktor.http.encodeURLParameter

fun String.appendParams(params: Map<String, String?>): String {
    val filtered = params.filterValues { !it.isNullOrBlank() }
    if (filtered.isEmpty()) return this
    // P0.5: encodea valores (espacios, comas, etc). Antes "leche entera" rompía la URL.
    return this + "?" + filtered.map { "${it.key}=${it.value!!.encodeURLParameter()}" }.joinToString("&")
}
