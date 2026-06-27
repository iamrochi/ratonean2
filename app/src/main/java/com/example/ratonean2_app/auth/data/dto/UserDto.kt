package com.example.ratonean2_app.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String?
)

@Serializable
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String
)

@Serializable
data class UpdateUserRequest(
    val name: String,
    val email: String
)