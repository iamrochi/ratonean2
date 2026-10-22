package com.example.ratonean2_app.core.di

import android.content.Context
import androidx.room.Room
import com.example.ratonean2_app.core.data.database.CatalogDatabase
import com.example.ratonean2_app.core.data.database.UserDatabase
import org.koin.dsl.module
import java.io.File

val databaseModule = module {

    // Catálogo nacional: archivo en noBackupFilesDir (excluido de backups
    // automáticamente) porque una DB de GBs rompería el backup y la cuota.
    // Destructive solo acá: es caché evictable, nunca datos de usuario.
    single {
        val context: Context = get()
        val dbFile = File(context.noBackupFilesDir, "catalog.db")
        Room.databaseBuilder(
            context,
            CatalogDatabase::class.java,
            dbFile.absolutePath
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // Usuario: DB chica en ubicación default (entra al backup).
    // Sin destructive: no hay copia en backend.
    single {
        Room.databaseBuilder(
            get(),
            UserDatabase::class.java,
            "user_db"
        ).build()
    }

    single { get<CatalogDatabase>().commerceDao() }
    single { get<CatalogDatabase>().branchDao() }
    single { get<CatalogDatabase>().productDao() }
    single { get<CatalogDatabase>().placeSearchCacheDao() }
    single { get<CatalogDatabase>().pendingWritesDao() }
    single { get<UserDatabase>().shoppingListDao() }
}
