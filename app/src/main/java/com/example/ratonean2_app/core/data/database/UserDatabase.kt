package com.example.ratonean2_app.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ratonean2_app.shoppinglist.data.local.ShoppingItemEntity
import com.example.ratonean2_app.shoppinglist.data.local.ShoppingListDao
import com.example.ratonean2_app.shoppinglist.data.local.ShoppingListEntity

/**
 * Datos del usuario (listas + items, KBs). Separada del catálogo para que:
 * - entre en el backup (Auto Backup + cloud-backup la incluyen),
 * - nunca la borre una migración/evicción del caché de productos,
 * - el prune del catálogo no la afecte (los items guardan su propia copia).
 * Sin fallback destructivo: son datos sin copia en backend.
 */
@Database(
    entities = [
        ShoppingListEntity::class,
        ShoppingItemEntity::class
    ],
    version = 1
)
abstract class UserDatabase : RoomDatabase() {

    abstract fun shoppingListDao(): ShoppingListDao
}
