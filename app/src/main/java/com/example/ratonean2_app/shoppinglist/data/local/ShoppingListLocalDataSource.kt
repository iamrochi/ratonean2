package com.example.ratonean2_app.shoppinglist.data.local

import com.example.ratonean2_app.product.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull


class ShoppingListLocalDataSource(private val dao: ShoppingListDao) {

    // --- GESTIÓN DE LISTAS ---

    fun getAllLists(): Flow<List<ShoppingListEntity>> = dao.getAllLists()

    suspend fun createList(name: String, setAsActive: Boolean = false) {
        val newList = ShoppingListEntity(name = name, isDefault = setAsActive)
        if (setAsActive) {
            dao.clearActiveStatus()
        }
        dao.insertList(newList)
    }

    suspend fun renameList(listId: String, newName: String) = dao.renameList(listId, newName)

    suspend fun setActiveList(listId: String) = dao.setActiveList(listId)

    suspend fun deleteList(listId: String) {
        dao.deleteList(listId)
    }

    // --- GESTIÓN DE ITEMS ---

    fun getItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
        dao.getItemsForList(listId)

    suspend fun addProductToActiveList(product: Product) {
        val activeList = dao.getActiveList() ?: createDefaultList()
        addItemToList(activeList.listId, product)
    }

    suspend fun addItemToList(listId: String, product: Product) {
        val existingItem = dao.getItemInList(listId, product.productId)

        if (existingItem != null) {
            dao.updateQuantity(existingItem.itemId, existingItem.quantity + 1)
        } else {
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
            dao.insertItem(newItem)
        }
    }

    suspend fun updateItemQuantity(itemId: String, quantity: Int) {
        if (quantity <= 0) {
            dao.deleteItem(itemId)
        } else {
            dao.updateQuantity(itemId, quantity)
        }
    }

    suspend fun toggleItemCheck(itemId: String, isChecked: Boolean) {
        dao.toggleCheck(itemId, isChecked)
    }

    suspend fun removeItem(itemId: String) = dao.deleteItem(itemId)

    suspend fun removeCheckedItems(listId: String) = dao.deleteCheckedItems(listId)


    private suspend fun createDefaultList(): ShoppingListEntity {
        val newList = ShoppingListEntity(name = "Mi Lista Principal", isDefault = true)
        dao.insertList(newList)
        return newList
    }
}