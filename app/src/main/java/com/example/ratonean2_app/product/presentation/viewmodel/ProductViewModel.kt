package com.example.ratonean2_app.product.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.core.network.toUserFriendlyMessage
import com.example.ratonean2_app.product.domain.model.Product
import com.example.ratonean2_app.product.domain.usecase.GetAllProductsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProductViewModel(
    private val getAllProductsUseCase: GetAllProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            getAllProductsUseCase().collect { response ->
                when (response) {
                    is NetworkResponse.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is NetworkResponse.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                products = response.data ?: emptyList()
                            )
                        }
                    }
                    is NetworkResponse.Failure -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = response.error.toUserFriendlyMessage()
                            )
                        }
                    }
                }
            }
        }
    }

    fun searchProducts(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(filteredProducts = emptyList()) }
            return
        }

        _uiState.update {
            it.copy(
                filteredProducts = it.products.filter { product ->
                    product.description.contains(query, ignoreCase = true)
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
