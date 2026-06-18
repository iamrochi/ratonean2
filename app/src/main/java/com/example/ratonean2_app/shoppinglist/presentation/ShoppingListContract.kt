package com.example.ratonean2_app.shoppinglist.presentation

import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingItem
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingList

data class ShoppingViewState(
    val lists: List<ShoppingList> = emptyList(),
    val selectedList: ShoppingList? = null,
    val items: List<ShoppingItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false
)

sealed class ShoppingIntent {
    data class SelectList(val listId: String) : ShoppingIntent()
    data class CreateList(val name: String) : ShoppingIntent()
    data class RenameList(val listId: String, val newName: String) : ShoppingIntent()
    data class DeleteList(val listId: String) : ShoppingIntent()

    data class UpdateQuantity(val itemId: String, val newQty: Int) : ShoppingIntent()
    data class ToggleItem(val itemId: String, val isChecked: Boolean) : ShoppingIntent()
    data class DeleteItem(val itemId: String) : ShoppingIntent()
    object ClearCheckedItems : ShoppingIntent()
}
