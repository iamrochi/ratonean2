package com.example.ratonean2_app.shoppinglist.data.local

import com.example.ratonean2_app.product.domain.model.Product
import kotlinx.coroutines.flow.Flow

class ShoppingListLocalDataSource(private val dao: ShoppingListDao) {

    fun getAllLists(): Flow<List<ShoppingListEntity>> = dao.getAllLists()

    fun getAllItems(): Flow<List<ShoppingItemEntity>> = dao.getAllItems()

    suspend fun createList(name: String, setAsActive: Boolean = false) {
        val newList = ShoppingListEntity(name = name.trim(), isDefault = setAsActive)
        // P0.4/M1: atómico. Antes era clearActiveStatus() + insert fuera de tx.
        if (setAsActive) {
            dao.createListAndActivate(newList)
        } else {
            dao.insertList(newList)
        }
    }

    suspend fun renameList(listId: String, newName: String) = dao.renameList(listId, newName)

    suspend fun setBudget(listId: String, budget: Double?) = dao.setBudget(listId, budget)

    suspend fun setColor(listId: String, color: String?) = dao.setColor(listId, color)

    suspend fun setActiveList(listId: String) = dao.setActiveList(listId)

    suspend fun deleteList(listId: String) = dao.deleteList(listId)

    fun getItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
        dao.getItemsForList(listId)

    fun getItemsForLists(listIds: List<String>): Flow<List<ShoppingItemEntity>> =
        dao.getItemsForLists(listIds)

    suspend fun addProductToActiveList(product: Product) {
        val activeList = dao.getActiveList() ?: createDefaultList()
        addItemToList(activeList.listId, product)
    }

    suspend fun addItemToList(listId: String, product: Product) {
        // P0.4/M2: upsert atómico en DAO. Sin read-modify-write en el datasource.
        val newItem = ShoppingItemEntity(
            listId = listId,
            productId = product.productId,
            description = product.description,
            brand = product.brand,
            imageUrl = product.imageUrl,
            price = product.listPrice,
            commerceId = product.commerceId,
            branchId = product.branchId
        )
        dao.upsertItem(listId, newItem)
    }

    suspend fun updateItemQuantity(itemId: String, quantity: Int) {
        if (quantity <= 0) dao.deleteItem(itemId)
        else dao.updateQuantity(itemId, quantity)
    }

    suspend fun toggleItemCheck(itemId: String, isChecked: Boolean) = dao.toggleCheck(itemId, isChecked)

    suspend fun updateNotes(itemId: String, notes: String?) = dao.updateNotes(itemId, notes)

    suspend fun updatePriority(itemId: String, priority: Int) = dao.updatePriority(itemId, priority)

    suspend fun removeItem(itemId: String) = dao.deleteItem(itemId)

    suspend fun removeCheckedItems(listId: String) = dao.deleteCheckedItems(listId)

    suspend fun getItemCount(listId: String) = dao.getItemCount(listId)

    suspend fun getCheckedCount(listId: String) = dao.getCheckedCount(listId)

    suspend fun getItemsForListOnce(listId: String) = dao.getItemsForListOnce(listId)

    private suspend fun createDefaultList(): ShoppingListEntity {
        val newList = ShoppingListEntity(name = "Mi Lista Principal", isDefault = true)
        dao.insertList(newList)
        return newList
    }
}
