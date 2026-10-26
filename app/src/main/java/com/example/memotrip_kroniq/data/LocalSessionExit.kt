package com.example.memotrip_kroniq.data

import com.example.memotrip_kroniq.data.datastore.AuthSessionStore
import kotlinx.coroutines.CancellationException

enum class LocalSessionExit { LOGOUT, REAUTHENTICATION }

/** Navigation is allowed only after a successful durable write; no destructive fallback. */
suspend fun finishLocalSession(
    store: AuthSessionStore,
    reason: LocalSessionExit,
    onComplete: () -> Unit,
    onStorageError: () -> Unit
) {
    try {
        when (reason) {
            LocalSessionExit.LOGOUT -> store.clearTokens()
            LocalSessionExit.REAUTHENTICATION -> store.requireReauthentication()
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        onStorageError()
        return
    }
    onComplete()
}
