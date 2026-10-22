package com.example.ratonean2_app.core.network

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter

/** Página móvil por defecto: 20 (antes 50, pesado para listados). */
const val DEFAULT_PAGE_SIZE = 20

fun HttpRequestBuilder.paginate(limit: Int = DEFAULT_PAGE_SIZE, offset: Int = 0) {
    parameter("limit", limit.coerceAtLeast(1))
    parameter("offset", offset.coerceAtLeast(0))
}
