package com.example.memotrip_kroniq.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.memotrip_kroniq.data.datastore.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.crypto.KeyGenerator

class LocalSessionExitTest {
    @get:Rule val temp = TemporaryFolder()
    private val user = "11111111-1111-4111-8111-111111111111"
    private fun session() = AuthSession("access", "refresh", "Bearer", 900, "2099-01-01T00:00:00.000Z",
        "22222222-2222-4222-8222-222222222222", user, System.currentTimeMillis())

    @Test fun failedLogoutOrReauthentication_preservesRecordShowsErrorAndDoesNotNavigate() = runBlocking {
        for (reason in LocalSessionExit.entries) {
            val job = SupervisorJob()
            try {
                val prefs = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) {
                    temp.newFile("${reason.name}.preferences_pb")
                }
                val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
                val delegate = AesGcmSessionCipher { key }
                var failWrites = false
                val cipher = object : SessionCipher {
                    override fun decrypt(ciphertext: ByteArray) = delegate.decrypt(ciphertext)
                    override fun encrypt(plaintext: ByteArray): ByteArray {
                        if (failWrites) throw java.io.IOException("Write unavailable")
                        return delegate.encrypt(plaintext)
                    }
                }
                val store = EncryptedAuthSessionStore(prefs, cipher)
                store.saveSession(session(), store.beginAuthentication())
                store.savePendingPayment(PendingPayment("pending", "PREMIUM", user))
                val ticket = store.beginAuthentication()
                val before = store.snapshot()
                var completions = 0
                var errors = 0
                failWrites = true
                finishLocalSession(store, reason, { completions++ }, { errors++ })
                assertEquals(0, completions)
                assertEquals(1, errors)
                assertEquals(before, store.snapshot())
                failWrites = false
                finishLocalSession(store, reason, { completions++ }, { errors++ })
                assertEquals(1, completions)
                assertEquals(1, errors)
                assertNull(store.snapshot().session)
                assertFalse(store.saveSession(session(), ticket))
                assertEquals(reason == LocalSessionExit.REAUTHENTICATION, store.snapshot().pendingPayments.containsKey(user))
            } finally { job.cancelAndJoin() }
        }
    }

    @Test fun unreadableStorage_reportsFailureWithoutDestructiveReset() = runBlocking {
        val real = AuthRepositorySessionTest.MemoryStore()
        val original = session()
        real.saveSession(original, real.beginAuthentication())
        real.savePendingPayment(PendingPayment("pending", "KRONIQ", user))
        val unreadable = object : AuthSessionStore by real {
            override suspend fun clearTokens() { throw javax.crypto.AEADBadTagException("Invalid record") }
            override suspend fun requireReauthentication() { throw javax.crypto.AEADBadTagException("Invalid record") }
        }
        for (reason in LocalSessionExit.entries) {
            var error = false
            finishLocalSession(unreadable, reason, { fail("Must not navigate") }, { error = true })
            assertTrue(error)
            assertEquals(original, real.snapshot().session)
            assertEquals("pending", real.snapshot().pendingPayments[user]!!.purchaseId)
        }
    }

    @Test fun cancellation_isPropagatedWithoutReportingSuccess() = runBlocking {
        val real = AuthRepositorySessionTest.MemoryStore()
        val cancelled = object : AuthSessionStore by real {
            override suspend fun clearTokens() { throw CancellationException() }
        }
        try {
            finishLocalSession(cancelled, LocalSessionExit.LOGOUT, { fail("Cancelled operation succeeded") }, { fail("Cancellation is not a storage error") })
            fail("Cancellation swallowed")
        } catch (_: CancellationException) {}
    }
}
