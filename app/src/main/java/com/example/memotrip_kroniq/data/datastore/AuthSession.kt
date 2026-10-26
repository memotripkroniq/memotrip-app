package com.example.memotrip_kroniq.data.datastore

import kotlinx.coroutines.flow.Flow

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val refreshTokenExpiresAt: String,
    val sessionId: String,
    val userId: String,
    val receivedAtEpochMillis: Long
) {
    val accessExpiresAtEpochMillis: Long get() = receivedAtEpochMillis + expiresIn * 1000
    fun hasUsableAccessToken(now: Long = System.currentTimeMillis()) = accessExpiresAtEpochMillis > now
    override fun toString() = "AuthSession(<redacted>)"
}

// Never persist a payment URL or its capability token.
data class PendingPayment(val purchaseId: String, val plan: String, val userId: String)

data class AuthStorageState(
    val generation: String = java.util.UUID.randomUUID().toString(),
    val session: AuthSession? = null,
    val pendingPayments: Map<String, PendingPayment> = emptyMap(),
    val legacyPendingImportAllowed: Boolean = false
) {
    override fun toString() = "AuthStorageState(<redacted>)"
}

/** Generation fences late login results; also suitable for a future refresh coordinator. */
interface AuthSessionStore {
    val session: Flow<AuthSession?>
    val accessToken: Flow<String?>
    suspend fun snapshot(): AuthStorageState
    suspend fun beginAuthentication(): String
    suspend fun saveSession(session: AuthSession, expectedGeneration: String): Boolean
    suspend fun clearTokens()
    suspend fun requireReauthentication()
    suspend fun savePendingPayment(payment: PendingPayment, expectedGeneration: String? = null): Boolean
    suspend fun importSavedPendingPayment(payment: PendingPayment, expectedGeneration: String): Boolean
    suspend fun clearPendingPayment(payment: PendingPayment)
}
