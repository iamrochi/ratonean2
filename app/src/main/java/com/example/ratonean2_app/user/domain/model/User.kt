package com.example.ratonean2_app.user.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int = 0,
    val name: String,
    val email: String
)
