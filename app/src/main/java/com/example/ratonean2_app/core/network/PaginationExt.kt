package com.example.ratonean2_app.core.network

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter

fun HttpRequestBuilder.paginate(limit: Int = 50, offset: Int = 0) {
    parameter("limit", limit)
    parameter("offset", offset)
}
