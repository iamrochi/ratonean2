package com.example.ratonean2_app

import android.app.Application
import com.example.ratonean2_app.auth.di.authModule
import com.example.ratonean2_app.branch.di.branchModule
import com.example.ratonean2_app.branch.detail.di.branchDetailModule
import com.example.ratonean2_app.commerce.di.commerceModule
import com.example.ratonean2_app.core.di.coreModule
import com.example.ratonean2_app.core.di.databaseModule
import com.example.ratonean2_app.core.di.networkModule
import com.example.ratonean2_app.map.di.locationModule
import com.example.ratonean2_app.places.di.placesModule
import com.example.ratonean2_app.product.di.productModule
import com.example.ratonean2_app.shoppinglist.di.shoppingListModule
import com.example.ratonean2_app.shoppinglist.presentation.compare.compareModule
import com.example.ratonean2_app.user.di.userModule
import com.example.ratonean2_app.auth.di.AuthManagerInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class Ratonean2App : Application() {
    override fun onCreate() {
        super.onCreate()
        com.example.ratonean2_app.core.work.AppContextHolder.appContext = this
        // Prune del caché + reintento de outbox (offline-first con mala conexión).
        com.example.ratonean2_app.core.work.OutboxSyncScheduler.schedulePeriodic(this)
        startKoin {
            androidContext(this@Ratonean2App)
            modules(
                listOf(
                    coreModule,
                    locationModule,
                    networkModule,
                    authModule,
                    userModule,
                    productModule,
                    commerceModule,
                    branchModule,
                    branchDetailModule,
                    placesModule,
                    databaseModule,
                    shoppingListModule,
                    compareModule,
                )
            )
            // P0.2: fuerza la hidratación del token persistido al arrancar.
            koin.get<AuthManagerInitializer>()
        }
    }
}