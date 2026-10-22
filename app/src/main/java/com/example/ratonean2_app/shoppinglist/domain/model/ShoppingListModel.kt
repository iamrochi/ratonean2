package com.example.ratonean2_app.shoppinglist.domain.model

data class ShoppingList(
    val id: String,
    val name: String,
    val isDefault: Boolean,
    val createdAt: Long,
    val budget: Double? = null,
    val color: String? = null,
    val itemCount: Int = 0,
    val checkedCount: Int = 0
) {
    val progress: Float
        get() = if (itemCount == 0) 0f else checkedCount.toFloat() / itemCount

    val budgetUsed: Float
        get() = if (budget == null || budget <= 0) 0f else 0f
}

data class ShoppingItem(
    val id: String,
    val listId: String,
    val productId: String,
    val description: String,
    val brand: String,
    val imageUrl: String?,
    val price: Double,
    val quantity: Int,
    val isChecked: Boolean,
    val commerceId: String,
    val branchId: String,
    val notes: String? = null,
    val priority: Int = 0,
    val category: String? = null
) {
    val subtotal: Double get() = price * quantity

    companion object {
        const val PRIORITY_NONE = 0
        const val PRIORITY_HIGH = 1
        const val PRIORITY_MEDIUM = 2
        const val PRIORITY_LOW = 3
    }
}
