package com.example.ratonean2_app.shoppinglist.domain.mapper

import com.example.ratonean2_app.product.domain.model.Product
import com.example.ratonean2_app.shoppinglist.data.local.ShoppingItemEntity
import com.example.ratonean2_app.shoppinglist.data.local.ShoppingListEntity
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingItem
import com.example.ratonean2_app.shoppinglist.domain.model.ShoppingList
import java.util.UUID

fun ShoppingListEntity.toDomain(itemCount: Int = 0, checkedCount: Int = 0) = ShoppingList(
    id = listId,
    name = name,
    isDefault = isDefault,
    createdAt = createdAt,
    budget = budget,
    color = color,
    itemCount = itemCount,
    checkedCount = checkedCount
)

fun ShoppingList.toEntity() = ShoppingListEntity(
    listId = id,
    name = name,
    isDefault = isDefault,
    createdAt = createdAt,
    budget = budget,
    color = color
)

fun ShoppingItemEntity.toDomain() = ShoppingItem(
    id = itemId,
    listId = listId,
    productId = productId,
    description = description,
    brand = brand,
    imageUrl = imageUrl,
    price = price,
    quantity = quantity,
    isChecked = isChecked,
    commerceId = commerceId,
    branchId = branchId,
    notes = notes,
    priority = priority,
    category = category
)

fun ShoppingItem.toEntity() = ShoppingItemEntity(
    itemId = id,
    listId = listId,
    productId = productId,
    description = description,
    brand = brand,
    imageUrl = imageUrl,
    price = price,
    quantity = quantity,
    isChecked = isChecked,
    commerceId = commerceId,
    branchId = branchId,
    notes = notes,
    priority = priority,
    category = category
)

fun Product.toShoppingItem(listId: String): ShoppingItem = ShoppingItem(
    id = UUID.randomUUID().toString(),
    listId = listId,
    productId = this.productId,
    description = this.description,
    brand = this.brand,
    imageUrl = this.imageUrl,
    price = this.listPrice,
    quantity = 1,
    isChecked = false,
    commerceId = this.commerceId,
    branchId = this.branchId,
    category = null
)
