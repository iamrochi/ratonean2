package com.example.ratonean2_app.shoppinglist.presentation

import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingItem
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingList

enum class ItemSortMode {
    DEFAULT,
    NAME_ASC,
    NAME_DESC,
    PRICE_ASC,
    PRICE_DESC,
    PRIORITY,
    CHECKED_LAST
}

data class ShoppingViewState(
    val lists: List<ShoppingList> = emptyList(),
    val selectedList: ShoppingList? = null,
    val items: List<ShoppingItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val totalQuantity: Int = 0,
    val checkedCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortMode: ItemSortMode = ItemSortMode.DEFAULT,
    val editingItemId: String? = null
) {
    val progress: Float
        get() = if (items.isEmpty()) 0f else checkedCount.toFloat() / items.size

    val budgetProgress: Float?
        get() {
            val budget = selectedList?.budget ?: return null
            if (budget <= 0) return null
            return (totalPrice / budget).coerceIn(0.0, 1.5).toFloat()
        }

    val sortedItems: List<ShoppingItem>
        get() = when (sortMode) {
            ItemSortMode.DEFAULT -> items
            ItemSortMode.NAME_ASC -> items.sortedBy { it.description.lowercase() }
            ItemSortMode.NAME_DESC -> items.sortedByDescending { it.description.lowercase() }
            ItemSortMode.PRICE_ASC -> items.sortedBy { it.price * it.quantity }
            ItemSortMode.PRICE_DESC -> items.sortedByDescending { it.price * it.quantity }
            ItemSortMode.PRIORITY -> items.sortedBy { it.priority }
            ItemSortMode.CHECKED_LAST -> items.sortedBy { it.isChecked }
        }
}

sealed class ShoppingIntent {
    data class SelectList(val listId: String) : ShoppingIntent()
    data class CreateList(val name: String) : ShoppingIntent()
    data class RenameList(val listId: String, val newName: String) : ShoppingIntent()
    data class DeleteList(val listId: String) : ShoppingIntent()
    data class SetBudget(val listId: String, val budget: Double?) : ShoppingIntent()
    data class SetColor(val listId: String, val color: String?) : ShoppingIntent()

    data class UpdateQuantity(val itemId: String, val newQty: Int) : ShoppingIntent()
    data class ToggleItem(val itemId: String, val isChecked: Boolean) : ShoppingIntent()
    data class DeleteItem(val itemId: String) : ShoppingIntent()
    data class UpdateNotes(val itemId: String, val notes: String?) : ShoppingIntent()
    data class UpdatePriority(val itemId: String, val priority: Int) : ShoppingIntent()
    data class SetSortMode(val sortMode: ItemSortMode) : ShoppingIntent()
    data class StartEditItem(val itemId: String?) : ShoppingIntent()
    object ClearCheckedItems : ShoppingIntent()
}
