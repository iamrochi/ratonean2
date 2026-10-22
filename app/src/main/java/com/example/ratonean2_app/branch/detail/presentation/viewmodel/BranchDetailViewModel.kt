package com.example.ratonean2_app.branch.detail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratonean2_app.core.network.DEFAULT_PAGE_SIZE
import com.example.ratonean2_app.core.network.toUserFriendlyMessage
import com.example.ratonean2_app.product.domain.model.Product
import com.example.ratonean2_app.product.domain.usecase.GetProductsByBranchKeyUseCase
import com.example.ratonean2_app.product.domain.usecase.GetProductsBySearchInBranchKeysUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class BranchDetailUiState(
    val branchKey: String = "",
    val branchName: String = "",
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val searchQuery: String = ""
)

class BranchDetailViewModel(
    private val getProductsByBranchKeyUseCase: GetProductsByBranchKeyUseCase,
    private val searchInBranchKeysUseCase: GetProductsBySearchInBranchKeysUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BranchDetailUiState())
    val uiState: StateFlow<BranchDetailUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null

    fun loadBranch(branchKey: String, branchName: String) {
        if (_uiState.value.branchKey == branchKey && _uiState.value.products.isNotEmpty()) return
        _uiState.update { it.copy(branchKey = branchKey, branchName = branchName) }
        fetchPage(branchKey, offset = 0, replace = true)
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.isLoading || s.isLoadingMore || !s.hasMore || s.branchKey.isBlank()) return
        fetchPage(s.branchKey, offset = s.products.size, replace = false)
    }

    // Antes matcheaba NetworkResponse sobre un Flow<List> (ramas muertas:
    // el loading/error jamás se emitían). Ahora estados reales + páginas.
    private fun fetchPage(branchKey: String, offset: Int, replace: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = replace,
                    isLoadingMore = !replace,
                    error = if (replace) null else it.error
                )
            }
            try {
                getProductsByBranchKeyUseCase(branchKey, DEFAULT_PAGE_SIZE, offset)
                    .catch { e ->
                        if (e is CancellationException) throw e
                        throw e
                    }
                    .collect { page ->
                        _uiState.update { state ->
                            val known = state.products.map { it.productId }.toSet()
                            val fresh = page.filter { it.productId !in known }
                            val all = if (replace) page else state.products + fresh
                            state.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                products = all,
                                filteredProducts = applyFilter(all, state.searchQuery),
                                hasMore = page.size >= DEFAULT_PAGE_SIZE
                            )
                        }
                    }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = e.message.toUserFriendlyMessage()
                    )
                }
            }
        }
    }

    // Búsqueda contra FTS local + red (antes filtro en memoria sobre todo
    // lo cargado: inviable con miles de productos por sucursal).
    fun search(query: String) {
        searchJob?.cancel()
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(filteredProducts = it.products) }
            return
        }
        val branchKey = _uiState.value.branchKey
        if (branchKey.isBlank()) return
        searchJob = viewModelScope.launch {
            delay(400)
            try {
                searchInBranchKeysUseCase(listOf(branchKey), query, DEFAULT_PAGE_SIZE, 0)
                    .collect { results ->
                        _uiState.update { it.copy(filteredProducts = results) }
                    }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                // Fallback honesto: filtro local sobre lo ya cargado.
                _uiState.update { state ->
                    state.copy(
                        filteredProducts = applyFilter(state.products, query),
                        error = e.message.toUserFriendlyMessage()
                    )
                }
            }
        }
    }

    private fun applyFilter(products: List<Product>, query: String): List<Product> {
        if (query.isBlank()) return products
        return products.filter { p ->
            p.description.contains(query, ignoreCase = true) ||
                p.brand.contains(query, ignoreCase = true)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
