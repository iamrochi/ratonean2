package com.example.ratonean2_app.user.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ratonean2_app.user.domain.model.UserLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferencesManager(private val context: Context) {

    private val LATITUDE = doublePreferencesKey("last_lat")
    private val LONGITUDE = doublePreferencesKey("last_lng")
    private val ADDRESS_NAME = stringPreferencesKey("last_address")
    private val IS_GPS = booleanPreferencesKey("is_gps")

    // P0.5/m3: DataStore corrupto ya no crashea, emite null.
    val userLocation: Flow<UserLocation?> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
        val lat = prefs[LATITUDE]
        val lng = prefs[LONGITUDE]
        if (lat != null && lng != null) {
            UserLocation(
                latitude = lat,
                longitude = lng,
                addressName = prefs[ADDRESS_NAME],
                isGpsBased = prefs[IS_GPS] ?: false
            )
        } else null
    }

    suspend fun saveLocation(location: UserLocation) {
        context.dataStore.edit { prefs ->
            prefs[LATITUDE] = location.latitude
            prefs[LONGITUDE] = location.longitude
            // P0.5/m3: null remueve la key en vez de guardar "" indistinguible.
            if (location.addressName == null) prefs.remove(ADDRESS_NAME)
            else prefs[ADDRESS_NAME] = location.addressName
            prefs[IS_GPS] = location.isGpsBased
        }
    }
}