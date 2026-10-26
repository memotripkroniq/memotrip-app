package com.example.memotrip_kroniq.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.util.UUID

class EncryptedAuthSessionStore(
    private val preferences: DataStore<Preferences>,
    private val cipher: SessionCipher
) : AuthSessionStore {
    private val gson = Gson()
    private val recordKey = stringPreferencesKey("encrypted_session_v1")
    private val legacyKeys = listOf("jwt_token", "access_token", "refresh_token").map(::stringPreferencesKey)

    private suspend fun removeLegacyTokens() {
        preferences.edit { prefs ->
            // Persist the initial generation too, so a SavedState migration can be fenced against logout.
            if (prefs[recordKey] == null) {
                val plaintext = gson.toJson(AuthStorageState(legacyPendingImportAllowed = legacyKeys.any { prefs[it] != null })).toByteArray(Charsets.UTF_8)
                try { prefs[recordKey] = encode(cipher.encrypt(plaintext)) }
                finally { plaintext.fill(0) }
            }
            legacyKeys.forEach { prefs.remove(it) }
        }
    }
    private fun encode(bytes: ByteArray) = bytes.joinToString("") { "%02x".format(it.toInt() and 255) }

    private fun decode(prefs: Preferences): AuthStorageState {
        val encoded = prefs[recordKey] ?: return AuthStorageState(legacyPendingImportAllowed = legacyKeys.any { prefs[it] != null })
        val bytes = encoded.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return gson.fromJson(cipher.decrypt(bytes).toString(Charsets.UTF_8), AuthStorageState::class.java)
            ?: error("Secure session is unavailable")
    }
    private suspend fun update(transform: (AuthStorageState) -> AuthStorageState) {
        preferences.edit { prefs ->
            val next = transform(decode(prefs))
            val plaintext = gson.toJson(next).toByteArray(Charsets.UTF_8)
            try {
                prefs[recordKey] = encode(cipher.encrypt(plaintext))
                legacyKeys.forEach { prefs.remove(it) }
            } finally { plaintext.fill(0) }
        }
    }
    override val session = preferences.data.onStart { removeLegacyTokens() }
        .map { decode(it).session }.distinctUntilChanged()
    override val accessToken = session.map { it?.accessToken }.distinctUntilChanged()

    override suspend fun snapshot(): AuthStorageState {
        removeLegacyTokens()
        return decode(preferences.data.first())
    }
    override suspend fun beginAuthentication(): String {
        val generation = UUID.randomUUID().toString()
        update { it.copy(generation = generation) }
        return generation
    }
    override suspend fun saveSession(session: AuthSession, expectedGeneration: String): Boolean {
        var saved = false
        update {
            if (it.generation != expectedGeneration) it else {
                saved = true
                it.copy(session = session, generation = UUID.randomUUID().toString())
            }
        }
        return saved
    }
    override suspend fun clearTokens() {
        // Explicit logout also removes every account's pending payment metadata.
        update { AuthStorageState() }
    }
    override suspend fun requireReauthentication() {
        update { it.copy(session = null, generation = UUID.randomUUID().toString(), legacyPendingImportAllowed = false) }
    }
    override suspend fun savePendingPayment(payment: PendingPayment, expectedGeneration: String?): Boolean {
        var saved = false
        update {
            if (it.session?.userId != payment.userId || (expectedGeneration != null && it.generation != expectedGeneration)) it else {
                saved = true
                it.copy(pendingPayments = it.pendingPayments + (payment.userId to payment), legacyPendingImportAllowed = false)
            }
        }
        return saved
    }
    override suspend fun importSavedPendingPayment(payment: PendingPayment, expectedGeneration: String): Boolean {
        var saved = false
        update {
            // Explicit logout and modern records permanently outrank a stale SavedStateHandle.
            if (!it.legacyPendingImportAllowed || it.generation != expectedGeneration ||
                (it.session != null && it.session.userId != payment.userId)) it else {
                saved = true
                it.copy(
                    pendingPayments = if (it.pendingPayments.containsKey(payment.userId)) it.pendingPayments
                        else it.pendingPayments + (payment.userId to payment),
                    legacyPendingImportAllowed = false
                )
            }
        }
        return saved
    }

    override suspend fun clearPendingPayment(payment: PendingPayment) {
        update {
            if (it.pendingPayments[payment.userId] != payment) it
            else it.copy(pendingPayments = it.pendingPayments - payment.userId)
        }
    }
}
