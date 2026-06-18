package com.example.ratonean2_app.shoppinglist.domain.repository

import com.example.ratonean2_app.product.domain.model.Product
import com.example.ratonean2_app.shoppinglist.data.local.ShoppingItemEntity
import com.example.ratonean2_app.shoppinglist.data.local.ShoppingListEntity
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingItem
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingList
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository{

    // Listas
    fun getLists(): Flow<List<ShoppingList>>
    suspend fun createList(name: String, setAsActive: Boolean)
    suspend fun renameList(listId: String, newName: String)
    suspend fun setActiveList(listId: String)
    suspend fun deleteList(listId: String)

    // Items
    fun getItemsForList(listId: String): Flow<List<ShoppingItem>>
    suspend fun addItemToActiveList(product: Product)
    suspend fun addItemToList(listId: String, product: Product)
    suspend fun updateItemQuantity(itemId: String, quantity: Int)
    suspend fun toggleItemCheck(itemId: String, isChecked: Boolean)
    suspend fun deleteItem(itemId: String)
    suspend fun deleteCheckedItems(listId: String)
}