package com.example.ratonean2_app.shoppinglist.domain.usecases

import com.example.ratonean2_app.product.domain.model.Product
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingItem
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingList
import com.example.ratonean2_app.shoppinglist.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow

class GetAllShoppingListsUseCase(private val repository: ShoppingListRepository) {
    operator fun invoke(): Flow<List<ShoppingList>> = repository.getLists()
}

class GetItemsFromListUseCase(private val repository: ShoppingListRepository) {
    operator fun invoke(listId: String): Flow<List<ShoppingItem>> = repository.getItemsForList(listId)
}

class GetItemsFromListsUseCase(private val repository: ShoppingListRepository) {
    operator fun invoke(listIds: List<String>): Flow<List<ShoppingItem>> = repository.getItemsForLists(listIds)
}

class GetListItemsOnceUseCase(private val repository: ShoppingListRepository) {
    suspend operator fun invoke(listId: String): List<ShoppingItem> = repository.getItemsForListOnce(listId)
}

class AddToActiveListUseCase(private val repository: ShoppingListRepository) {
    suspend operator fun invoke(product: Product) = repository.addItemToActiveList(product)
}

class SetActiveListUseCase(private val repository: ShoppingListRepository) {
    suspend operator fun invoke(listId: String) = repository.setActiveList(listId)
}

class ManageShoppingItemUseCase(private val repository: ShoppingListRepository) {
    suspend fun updateQuantity(itemId: String, qty: Int) = repository.updateItemQuantity(itemId, qty)
    suspend fun toggleCheck(itemId: String, checked: Boolean) = repository.toggleItemCheck(itemId, checked)
    suspend fun delete(itemId: String) = repository.deleteItem(itemId)
    suspend fun clearChecked(listId: String) = repository.deleteCheckedItems(listId)
    suspend fun updateNotes(itemId: String, notes: String?) = repository.updateNotes(itemId, notes)
    suspend fun updatePriority(itemId: String, priority: Int) = repository.updatePriority(itemId, priority)
}

class ManageShoppingListUseCase(private val repository: ShoppingListRepository) {
    suspend fun create(name: String, active: Boolean = false) = repository.createList(name, active)
    suspend fun rename(id: String, name: String) = repository.renameList(id, name)
    suspend fun delete(id: String) = repository.deleteList(id)
    suspend fun setBudget(id: String, budget: Double?) = repository.setBudget(id, budget)
    suspend fun setColor(id: String, color: String?) = repository.setColor(id, color)
}
