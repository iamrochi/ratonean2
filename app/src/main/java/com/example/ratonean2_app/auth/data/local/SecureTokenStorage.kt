package com.example.ratonean2_app.auth.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * P0.2: persistencia cifrada del token. Sobrevive a muerte de proceso.
 * No guardar nunca el token en prefs planas ni solo en RAM.
 */
class SecureTokenStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _tokenFlow = MutableStateFlow(prefs.getString(KEY_TOKEN, null))
    val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

    fun getToken(): String? = _tokenFlow.value

    fun saveToken(token: String, userId: Int?) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .apply { if (userId != null) putInt(KEY_USER_ID, userId) else remove(KEY_USER_ID) }
            .apply()
        _tokenFlow.value = token
    }

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USER_ID).apply()
        _tokenFlow.value = null
    }

    fun getUserId(): Int? =
        if (prefs.contains(KEY_USER_ID)) prefs.getInt(KEY_USER_ID, -1).takeIf { it >= 0 } else null

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "auth_user_id"
    }
}
