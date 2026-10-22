package com.example.ratonean2_app.shoppinglist.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists ORDER BY createdAt DESC")
    fun getAllLists(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE isDefault = 1 LIMIT 1")
    suspend fun getActiveList(): ShoppingListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingListEntity)

    // P0.4/M1: creación + activación atómica. Evita doble lista activa con doble tap.
    @Transaction
    suspend fun createListAndActivate(list: ShoppingListEntity) {
        clearActiveStatus()
        insertList(list)
    }

    @Query("UPDATE shopping_lists SET isDefault = 0")
    suspend fun clearActiveStatus()

    @Query("UPDATE shopping_lists SET isDefault = 1 WHERE listId = :listId")
    suspend fun markListAsActive(listId: String)

    @Transaction
    suspend fun setActiveList(listId: String) {
        clearActiveStatus()
        markListAsActive(listId)
    }

    @Query("UPDATE shopping_lists SET name = :newName WHERE listId = :listId")
    suspend fun renameList(listId: String, newName: String)

    @Query("UPDATE shopping_lists SET budget = :budget WHERE listId = :listId")
    suspend fun setBudget(listId: String, budget: Double?)

    @Query("UPDATE shopping_lists SET color = :color WHERE listId = :listId")
    suspend fun setColor(listId: String, color: String?)

    @Query("DELETE FROM shopping_lists WHERE listId = :listId")
    suspend fun deleteList(listId: String)

    @Query("SELECT * FROM shopping_items WHERE listId = :listId")
    fun getItemsForList(listId: String): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE listId IN (:listIds)")
    fun getItemsForLists(listIds: List<String>): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE listId = :listId AND productId = :productId LIMIT 1")
    suspend fun getItemInList(listId: String, productId: String): ShoppingItemEntity?

    // P0.4/M2: upsert atómico de item (read + write en una transacción).
    @Transaction
    suspend fun upsertItem(listId: String, newItem: ShoppingItemEntity) {
        val existing = getItemInList(listId, newItem.productId)
        if (existing != null) {
            incrementQuantity(existing.itemId)
        } else {
            insertItem(newItem)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET quantity = :qty WHERE itemId = :itemId")
    suspend fun updateQuantity(itemId: String, qty: Int)

    // P0.4/M2: incremento atómico. Sin read-modify-write → sin qty perdida ni duplicados.
    @Query("UPDATE shopping_items SET quantity = quantity + 1 WHERE itemId = :itemId")
    suspend fun incrementQuantity(itemId: String)

    // P0.4/B3: un solo stream con todos los items para calcular counts en memoria (2 queries, no 2N).
    @Query("SELECT * FROM shopping_items")
    fun getAllItems(): Flow<List<ShoppingItemEntity>>

    @Query("UPDATE shopping_items SET isChecked = :checked WHERE itemId = :itemId")
    suspend fun toggleCheck(itemId: String, checked: Boolean)

    @Query("UPDATE shopping_items SET notes = :notes WHERE itemId = :itemId")
    suspend fun updateNotes(itemId: String, notes: String?)

    @Query("UPDATE shopping_items SET priority = :priority WHERE itemId = :itemId")
    suspend fun updatePriority(itemId: String, priority: Int)

    @Query("DELETE FROM shopping_items WHERE itemId = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("DELETE FROM shopping_items WHERE listId = :listId AND isChecked = 1")
    suspend fun deleteCheckedItems(listId: String)

    @Query("SELECT COUNT(*) FROM shopping_items WHERE listId = :listId")
    suspend fun getItemCount(listId: String): Int

    @Query("SELECT COUNT(*) FROM shopping_items WHERE listId = :listId AND isChecked = 1")
    suspend fun getCheckedCount(listId: String): Int

    @Query("SELECT * FROM shopping_items WHERE listId = :listId")
    suspend fun getItemsForListOnce(listId: String): List<ShoppingItemEntity>
}
