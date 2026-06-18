package com.example.ratonean2_app.shoppinglist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            when (intent) {
                is ShoppingIntent.SelectList -> setActiveListUseCase(intent.listId)

                is ShoppingIntent.ToggleItem ->
                    manageItemUseCase.toggleCheck(intent.itemId, intent.isChecked)

                is ShoppingIntent.UpdateQuantity ->
                    manageItemUseCase.updateQuantity(intent.itemId, intent.newQty)

                is ShoppingIntent.DeleteItem ->
                    manageItemUseCase.delete(intent.itemId)

                is ShoppingIntent.CreateList ->
                    manageListUseCase.create(intent.name, active = true)

                is ShoppingIntent.DeleteList ->
                    manageListUseCase.delete(intent.listId)

                is ShoppingIntent.RenameList ->
                    manageListUseCase.rename(intent.listId, intent.newName)

                ShoppingIntent.ClearCheckedItems -> {
                    state.value.selectedList?.let { list ->
                        manageItemUseCase.clearChecked(list.id)
                    }
                }
            }
        }
    }

    private fun observeLists() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllListsUseCase().collect { allLists ->
                val activeList = allLists.find { it.isDefault } ?: allLists.firstOrNull()

                _state.update { it.copy(
                    lists = allLists,
                    selectedList = activeList,
                    isLoading = false
                ) }

                activeList?.let { observeItems(it.id) }
            }
        }
    }

    private fun observeItems(listId: String) {
        itemsJob?.cancel()
        itemsJob = viewModelScope.launch {
            getItemsFromListUseCase(listId).collect { items ->
                val total = items.sumOf { it.price * it.quantity }
                _state.update { it.copy(
                    items = items,
                    totalPrice = total
                ) }
            }
        }
    }
}