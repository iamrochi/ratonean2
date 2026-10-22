package com.example.ratonean2_app.shoppinglist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratonean2_app.core.network.toUserFriendlyMessage
import com.example.ratonean2_app.shoppinglist.domain.usecases.GetAllShoppingListsUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.GetItemsFromListUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.ManageShoppingItemUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.ManageShoppingListUseCase
import com.example.ratonean2_app.shoppinglist.domain.usecases.SetActiveListUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ShoppingListViewModel(
    private val getAllListsUseCase: GetAllShoppingListsUseCase,
    private val getItemsFromListUseCase: GetItemsFromListUseCase,
    private val setActiveListUseCase: SetActiveListUseCase,
    private val manageItemUseCase: ManageShoppingItemUseCase,
    private val manageListUseCase: ManageShoppingListUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ShoppingViewState())
    val state: StateFlow<ShoppingViewState> = _state.asStateFlow()

    private var itemsJob: Job? = null

    init {
        observeLists()
    }

    fun onIntent(intent: ShoppingIntent) {
        viewModelScope.launch {
            try {
                when (intent) {
                    is ShoppingIntent.SelectList -> setActiveListUseCase(intent.listId)

                    is ShoppingIntent.ToggleItem -> {
                        manageItemUseCase.toggleCheck(intent.itemId, intent.isChecked)
                    }

                    is ShoppingIntent.UpdateQuantity ->
                        manageItemUseCase.updateQuantity(intent.itemId, intent.newQty)

                    is ShoppingIntent.DeleteItem ->
                        manageItemUseCase.delete(intent.itemId)

                    is ShoppingIntent.UpdateNotes ->
                        manageItemUseCase.updateNotes(intent.itemId, intent.notes)

                    is ShoppingIntent.UpdatePriority ->
                        manageItemUseCase.updatePriority(intent.itemId, intent.priority)

                    is ShoppingIntent.CreateList ->
                        manageListUseCase.create(intent.name, active = true)

                    is ShoppingIntent.DeleteList ->
                        manageListUseCase.delete(intent.listId)

                    is ShoppingIntent.RenameList ->
                        manageListUseCase.rename(intent.listId, intent.newName)

                    is ShoppingIntent.SetBudget ->
                        manageListUseCase.setBudget(intent.listId, intent.budget)

                    is ShoppingIntent.SetColor ->
                        manageListUseCase.setColor(intent.listId, intent.color)

                    is ShoppingIntent.SetSortMode ->
                        _state.update { it.copy(sortMode = intent.sortMode) }

                    is ShoppingIntent.StartEditItem ->
                        _state.update { it.copy(editingItemId = intent.itemId) }

                    ShoppingIntent.ClearCheckedItems -> {
                        state.value.selectedList?.let { list ->
                            manageItemUseCase.clearChecked(list.id)
                        }
                    }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _state.update { it.copy(error = e.message.toUserFriendlyMessage()) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun observeLists() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getAllListsUseCase().collect { allLists ->
                    val activeList = allLists.find { it.isDefault } ?: allLists.firstOrNull()
                    _state.update {
                        it.copy(
                            lists = allLists,
                            // P0.5: si no hay listas, limpiar selección e items rancios.
                            selectedList = activeList,
                            items = if (activeList == null) emptyList() else it.items,
                            isLoading = false,
                            error = null
                        )
                    }
                    if (activeList != null) observeItems(activeList.id)
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message.toUserFriendlyMessage())
                }
            }
        }
    }

    private fun observeItems(listId: String) {
        itemsJob?.cancel()
        itemsJob = viewModelScope.launch {
            try {
                getItemsFromListUseCase(listId).collect { items ->
                    val total = items.sumOf { it.price * it.quantity }
                    val totalQty = items.sumOf { it.quantity }
                    val checked = items.count { it.isChecked }
                    _state.update {
                        it.copy(
                            items = items,
                            totalPrice = total,
                            totalQuantity = totalQty,
                            checkedCount = checked
                        )
                    }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _state.update { it.copy(error = e.message.toUserFriendlyMessage()) }
            }
        }
    }
}
