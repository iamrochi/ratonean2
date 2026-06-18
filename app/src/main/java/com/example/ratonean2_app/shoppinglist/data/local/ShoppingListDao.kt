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

    @Query("DELETE FROM shopping_lists WHERE listId = :listId")
    suspend fun deleteList(listId: String)

    @Query("SELECT * FROM shopping_items WHERE listId = :listId")
    fun getItemsForList(listId: String): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE listId = :listId AND productId = :productId LIMIT 1")
    suspend fun getItemInList(listId: String, productId: String): ShoppingItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET quantity = :qty WHERE itemId = :itemId")
    suspend fun updateQuantity(itemId: String, qty: Int)

    @Query("UPDATE shopping_items SET isChecked = :checked WHERE itemId = :itemId")
    suspend fun toggleCheck(itemId: String, checked: Boolean)

    @Query("DELETE FROM shopping_items WHERE itemId = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("DELETE FROM shopping_items WHERE listId = :listId AND isChecked = 1")
    suspend fun deleteCheckedItems(listId: String)
}