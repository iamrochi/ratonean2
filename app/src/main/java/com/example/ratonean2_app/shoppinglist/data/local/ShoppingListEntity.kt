package com.example.ratonean2_app.shoppinglist.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey val listId: String = UUID.randomUUID().toString(),
    val name: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val budget: Double? = null,
    val color: String? = null
)

@Entity(
    tableName = "shopping_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["listId"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // P0.3/M5: evita full-scan + acelera getItemInList / counts.
    indices = [Index("listId"), Index(value = ["listId", "productId"])]
)
data class ShoppingItemEntity(
    @PrimaryKey val itemId: String = UUID.randomUUID().toString(),
    val listId: String,
    val productId: String,
    val description: String,
    val brand: String,
    val imageUrl: String?,
    val price: Double,
    val quantity: Int = 1,
    val isChecked: Boolean = false,
    val commerceId: String,
    val branchId: String,
    val notes: String? = null,
    val priority: Int = 0,
    val category: String? = null
)
