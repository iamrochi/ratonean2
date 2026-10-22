package com.example.ratonean2_app.shoppinglist.presentation.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratonean2_app.core.network.toUserFriendlyMessage
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingItem
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingList
import com.example.ratonean2_app.shoppinglist.domain.usecases.GetAllShoppingListsUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.GetListItemsOnceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class CompareUiState(
    val lists: List<ShoppingList> = emptyList(),
    val listAId: String? = null,
    val listBId: String? = null,
    val itemsA: List<ShoppingItem> = emptyList(),
    val itemsB: List<ShoppingItem> = emptyList(),
    val sharedItems: List<SharedItem> = emptyList(),
    val uniqueToA: List<ShoppingItem> = emptyList(),
    val uniqueToB: List<ShoppingItem> = emptyList(),
    val priceDiffA: Double = 0.0,
    val priceDiffB: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val summary: String
        get() = buildString {
            append("${sharedItems.size} en común")
            if (uniqueToA.isNotEmpty()) append(" · ${uniqueToA.size} solo en A")
            if (uniqueToB.isNotEmpty()) append(" · ${uniqueToB.size} solo en B")
        }
}

data class SharedItem(
    val description: String,
    val brand: String,
    val priceA: Double,
    val priceB: Double,
    val quantityA: Int,
    val quantityB: Int
) {
    val difference: Double get() = priceB - priceA
    val isCheaperInA: Boolean get() = difference > 0
    val isCheaperInB: Boolean get() = difference < 0
}

sealed class CompareIntent {
    data class SelectListA(val listId: String) : CompareIntent()
    data class SelectListB(val listId: String) : CompareIntent()
    data object Compare : CompareIntent()
}

class CompareListsViewModel(
    private val getAllListsUseCase: GetAllShoppingListsUseCase,
    private val getListItemsOnceUseCase: GetListItemsOnceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CompareUiState())
    val state: StateFlow<CompareUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getAllListsUseCase().collect { lists ->
                    _state.update { it.copy(lists = lists, isLoading = false) }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message.toUserFriendlyMessage()) }
            }
        }
    }

    fun onIntent(intent: CompareIntent) {
        viewModelScope.launch {
            try {
                when (intent) {
                    is CompareIntent.SelectListA -> _state.update { it.copy(listAId = intent.listId) }
                    is CompareIntent.SelectListB -> _state.update { it.copy(listBId = intent.listId) }
                    is CompareIntent.Compare -> compareLists()
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message.toUserFriendlyMessage()) }
            }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }

    private suspend fun compareLists() {
        val aId = _state.value.listAId ?: return
        val bId = _state.value.listBId ?: return
        if (aId == bId) {
            _state.update { it.copy(error = "Elegí dos listas distintas para comparar.") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        try {
            val itemsA = getListItemsOnceUseCase(aId)
            val itemsB = getListItemsOnceUseCase(bId)

            val mapA = itemsA.groupBy { it.description.lowercase().trim() }
            val mapB = itemsB.groupBy { it.description.lowercase().trim() }

            val sharedKeys = mapA.keys.intersect(mapB.keys)
            // P0.6: sin !!. mapNotNull es seguro aunque el groupBy cambie por locale.
            val sharedItems = sharedKeys.mapNotNull { key ->
                val a = mapA[key]?.firstOrNull()
                val b = mapB[key]?.firstOrNull()
                if (a == null || b == null) null else SharedItem(
                    description = a.description,
                    brand = a.brand,
                    priceA = a.price,
                    priceB = b.price,
                    quantityA = a.quantity,
                    quantityB = b.quantity
                )
            }

            val uniqueAKeys = mapA.keys - mapB.keys
            val uniqueBKeys = mapB.keys - mapA.keys

            val uniqueToA = uniqueAKeys.mapNotNull { mapA[it]?.firstOrNull() }
            val uniqueToB = uniqueBKeys.mapNotNull { mapB[it]?.firstOrNull() }

            _state.update {
                it.copy(
                    itemsA = itemsA,
                    itemsB = itemsB,
                    sharedItems = sharedItems,
                    uniqueToA = uniqueToA,
                    uniqueToB = uniqueToB,
                    priceDiffA = itemsA.sumOf { item -> item.price * item.quantity },
                    priceDiffB = itemsB.sumOf { item -> item.price * item.quantity },
                    isLoading = false
                )
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            // P0.6: antes isLoading quedaba en true eterno (spinner bloqueante).
            _state.update { it.copy(isLoading = false, error = e.message.toUserFriendlyMessage()) }
        }
    }
}
