package com.example.ratonean2_app.shoppinglist.data.repository

import com.example.ratonean2_app.product.domain.model.Product
import com.example.ratonean2_app.shoppinglist.data.local.ShoppingListLocalDataSource
import com.example.ratonean2_app.shoppinglist.domain.mapper.toDomain
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingItem
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingList
import com.example.ratonean2_app.shoppinglist.domain.repository.ShoppingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ShoppingListRepositoryImpl(
    private val localDataSource: ShoppingListLocalDataSource
) : ShoppingListRepository {

    // P0.4/B3: antes N+1 (2 queries suspend por lista dentro del map).
    // Ahora 2 streams combinados: listas + todos los items, counts en memoria.
    override fun getLists(): Flow<List<ShoppingList>> =
        combine(
            localDataSource.getAllLists(),
            localDataSource.getAllItems()
        ) { lists, items ->
            val counts = items.groupingBy { it.listId }.eachCount()
            val checked = items.filter { it.isChecked }.groupingBy { it.listId }.eachCount()
            lists.map { entity ->
                entity.toDomain(
                    itemCount = counts[entity.listId] ?: 0,
                    checkedCount = checked[entity.listId] ?: 0
                )
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun createList(name: String, setAsActive: Boolean) =
        localDataSource.createList(name, setAsActive)

    override suspend fun renameList(listId: String, newName: String) =
        localDataSource.renameList(listId, newName)

    override suspend fun setBudget(listId: String, budget: Double?) =
        localDataSource.setBudget(listId, budget)

    override suspend fun setColor(listId: String, color: String?) =
        localDataSource.setColor(listId, color)

    override suspend fun setActiveList(listId: String) =
        localDataSource.setActiveList(listId)

    override suspend fun deleteList(listId: String) =
        localDataSource.deleteList(listId)

    override fun getItemsForList(listId: String): Flow<List<ShoppingItem>> =
        localDataSource.getItemsForList(listId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getItemsForLists(listIds: List<String>): Flow<List<ShoppingItem>> =
        localDataSource.getItemsForLists(listIds).map { entities ->
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

    override suspend fun updateNotes(itemId: String, notes: String?) =
        localDataSource.updateNotes(itemId, notes)

    override suspend fun updatePriority(itemId: String, priority: Int) =
        localDataSource.updatePriority(itemId, priority)

    override suspend fun deleteItem(itemId: String) =
        localDataSource.removeItem(itemId)

    override suspend fun deleteCheckedItems(listId: String) =
        localDataSource.removeCheckedItems(listId)

    override suspend fun getItemCount(listId: String) =
        localDataSource.getItemCount(listId)

    override suspend fun getCheckedCount(listId: String) =
        localDataSource.getCheckedCount(listId)

    override suspend fun getItemsForListOnce(listId: String) =
        localDataSource.getItemsForListOnce(listId).map { it.toDomain() }
}
