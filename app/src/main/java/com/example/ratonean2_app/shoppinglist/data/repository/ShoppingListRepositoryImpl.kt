package com.example.ratonean2_app.shoppinglist.data.repository

import com.example.ratonean2_app.product.domain.model.Product
import com.example.ratonean2_app.shoppinglist.data.local.ShoppingItemEntity
import com.example.ratonean2_app.shoppinglist.data.local.ShoppingListEntity
import com.example.ratonean2_app.shoppinglist.data.local.ShoppingListLocalDataSource
import com.example.ratonean2_app.shoppinglist.domain.mapper.toDomain
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingItem
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingList
import com.example.ratonean2_app.shoppinglist.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShoppingListRepositoryImpl(
    private val localDataSource: ShoppingListLocalDataSource
) : ShoppingListRepository {

    override fun getLists(): Flow<List<ShoppingList>> =
        localDataSource.getAllLists().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun createList(name: String, setAsActive: Boolean) =
        localDataSource.createList(name, setAsActive)

    override suspend fun renameList(listId: String, newName: String) =
        localDataSource.renameList(listId, newName)

    override suspend fun setActiveList(listId: String) =
        localDataSource.setActiveList(listId)

    override suspend fun deleteList(listId: String) =
        localDataSource.deleteList(listId)

    override fun getItemsForList(listId: String): Flow<List<ShoppingItem>> =
        localDataSource.getItemsForList(listId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addItemToActiveList(product: Product) =
        localDataSource.addProductToActiveList(product)

    override suspend fun addItemToList(listId: String, product: Product) =
        localDataSource.addItemToList(listId, product)

    override suspend fun updateItemQuantity(itemId: String, quantity: Int) =
        localDataSource.updateItemQuantity(itemId, quantity)

    override suspend fun toggleItemCheck(itemId: String, isChecked: Boolean) =
        localDataSource.toggleItemCheck(itemId, isChecked)

    override suspend fun deleteItem(itemId: String) =
        localDataSource.removeItem(itemId)

    override suspend fun deleteCheckedItems(listId: String) =
        localDataSource.removeCheckedItems(listId)
}