package com.example.ratonean2_app.map.presentation.state

sealed interface SearchStatus {

    object Empty : SearchStatus
    object Idle : SearchStatus
    object Loading : SearchStatus
    object Success : SearchStatus
    data class Error(val message: String) : SearchStatus
}