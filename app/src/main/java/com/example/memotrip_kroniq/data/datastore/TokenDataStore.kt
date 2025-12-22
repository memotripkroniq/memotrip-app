package com.example.memotrip_kroniq.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// --- EXTENSION NA CONTEXT ---
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenDataStore(private val context: Context) {

    companion object {
        // starý token (používá login s emailem/heslem)
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")

        // nové klíče (používá Google login)
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    // starý getter — necháváme kvůli kompatibilitě
    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    // uloží token pro klasický login (email + heslo)
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    // uloží tokeny pro Google login
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    // getters pro Google login
    val accessToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ACCESS_TOKEN]
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[REFRESH_TOKEN]
    }

    // clear všeho při logoutu
    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.clear()  // smaže všechny hodnoty
        }
    }

    suspend fun getAccessToken(): String? {
        return context.dataStore.data
            .first()[ACCESS_TOKEN]
    }

    // 🔐 ULOŽENÍ TOKENU
    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = token
        }
    }
}
