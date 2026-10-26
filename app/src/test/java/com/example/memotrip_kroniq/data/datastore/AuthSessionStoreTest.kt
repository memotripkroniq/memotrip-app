package com.example.memotrip_kroniq.data.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.crypto.KeyGenerator

class AuthSessionStoreTest {
    @get:Rule val temp = TemporaryFolder()
    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    private val cipher = AesGcmSessionCipher { key }
    private val userA = "11111111-1111-4111-8111-111111111111"
    private val userB = "22222222-2222-4222-8222-222222222222"
    private fun session(user: String = userA) = AuthSession("access-$user", "refresh-$user", "Bearer", 900,
        "2099-01-01T00:00:00.000Z", "33333333-3333-4333-8333-333333333333", user, 1000)

    @Test fun aesGcm_encryptsWithFreshIvAndRejectsTampering() {
        val plain = "private token".toByteArray()
        val first = cipher.encrypt(plain)
        assertFalse(first.contentEquals(cipher.encrypt(plain)))
        assertArrayEquals(plain, cipher.decrypt(first))
        first[first.lastIndex] = (first.last().toInt() xor 1).toByte()
        try { cipher.decrypt(first); fail("Tampered ciphertext accepted") } catch (_: java.security.GeneralSecurityException) {}
    }

    @Test fun migration_discardsAllLegacyCombinationsWithoutCombiningUsers() = runBlocking {
        for (keys in listOf(listOf("jwt_token"), listOf("access_token"), listOf("access_token", "refresh_token"),
            listOf("jwt_token", "access_token", "refresh_token"))) {
            val job = SupervisorJob()
            try {
                val prefs = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) {
                    temp.newFile("migration-${keys.size}-${keys.first()}.preferences_pb")
                }
                prefs.edit { p -> keys.forEach { p[stringPreferencesKey(it)] = "legacy-credential" } }
                val store = EncryptedAuthSessionStore(prefs, cipher)
                assertNull(store.snapshot().session)
                assertTrue(prefs.data.first().asMap().keys.none { it.name in keys })
                assertNull(store.accessToken.first())
            } finally { job.cancelAndJoin() }
        }
    }

    @Test fun restart_restoresWholeSessionAndPendingWithoutPlaintextOnDisk() = runBlocking {
        val file = temp.newFile("restart.preferences_pb")
        var job = SupervisorJob()
        var prefs = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
        var store = EncryptedAuthSessionStore(prefs, cipher)
        val expected = session()
        assertTrue(store.saveSession(expected, store.beginAuthentication()))
        val pending = PendingPayment("purchase-1", "KRONIQ", userA)
        assertTrue(store.savePendingPayment(pending))
        assertFalse(file.readText().contains(expected.accessToken))
        assertFalse(file.readText().contains("purchase-1"))
        job.cancelAndJoin()
        job = SupervisorJob()
        try {
            prefs = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
            store = EncryptedAuthSessionStore(prefs, cipher)
            assertEquals(expected, store.session.first())
            assertEquals(pending, store.snapshot().pendingPayments[userA])
            assertFalse(file.readText().contains("paymentUrl"))
        } finally { job.cancelAndJoin() }
    }

    @Test fun concurrentLoginsAndLogout_cannotResurrectOrMixSessions() = runBlocking {
        val job = SupervisorJob()
        try {
            val prefs = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) {
                temp.newFile("race.preferences_pb")
            }
            val store = EncryptedAuthSessionStore(prefs, cipher)
            val attemptA = store.beginAuthentication()
            val attemptB = store.beginAuthentication()
            assertTrue(store.saveSession(session(userB), attemptB))
            assertFalse(store.saveSession(session(userA), attemptA))
            assertEquals(session(userB), store.snapshot().session)
            assertFalse(store.savePendingPayment(PendingPayment("old", "PREMIUM", userA)))
            val staleGeneration = store.snapshot().generation
            val late = store.beginAuthentication()
            store.clearTokens()
            assertFalse(store.saveSession(session(userA), late))
            assertNull(store.snapshot().session)
            store.saveSession(session(userB), store.beginAuthentication())
            assertFalse(store.savePendingPayment(PendingPayment("old", "PREMIUM", userB), staleGeneration))
        } finally { job.cancelAndJoin() }
    }

    @Test fun loginOfAnotherUser_hidesButPreservesPendingUntilExplicitLogout() = runBlocking {
        val job = SupervisorJob()
        try {
            val prefs = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) {
                temp.newFile("accounts.preferences_pb")
            }
            val store = EncryptedAuthSessionStore(prefs, cipher)
            store.saveSession(session(), store.beginAuthentication())
            val pending = PendingPayment("pending", "PREMIUM", userA)
            store.savePendingPayment(pending)
            store.saveSession(session(userB), store.beginAuthentication())
            val other = store.snapshot()
            assertNull(other.pendingPayments[other.session!!.userId])
            store.saveSession(session(userA), store.beginAuthentication())
            assertEquals(pending, store.snapshot().pendingPayments[userA])
            store.clearTokens()
            assertTrue(store.snapshot().pendingPayments.isEmpty())
        } finally { job.cancelAndJoin() }
    }

    @Test fun savedPaymentMigration_survivesSignInButCannotRunAfterLogout() = runBlocking {
        val job = SupervisorJob()
        try {
            val prefs = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) {
                temp.newFile("saved-payment.preferences_pb")
            }
            prefs.edit { it[stringPreferencesKey("jwt_token")] = "legacy-token" }
            val store = EncryptedAuthSessionStore(prefs, cipher)
            val before = store.snapshot()
            val pending = PendingPayment("legacy-purchase", "PREMIUM", userA)
            assertTrue(store.importSavedPendingPayment(pending, before.generation))
            assertFalse(store.importSavedPendingPayment(pending.copy(purchaseId = "stale"), store.snapshot().generation))
            store.saveSession(session(), store.beginAuthentication())
            assertEquals(pending, store.snapshot().pendingPayments[userA])
            store.clearTokens()
            assertFalse(store.importSavedPendingPayment(pending, before.generation))
            val reopened = EncryptedAuthSessionStore(prefs, cipher)
            assertFalse(reopened.importSavedPendingPayment(pending, reopened.snapshot().generation))
            assertTrue(reopened.snapshot().pendingPayments.isEmpty())
        } finally { job.cancelAndJoin() }
    }

    @Test fun passwordChange_keepsPendingAcrossStoreRecreationButInvalidatesLateLogin() = runBlocking {
        val job = SupervisorJob()
        try {
            val prefs = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) {
                temp.newFile("reauth.preferences_pb")
            }
            val store = EncryptedAuthSessionStore(prefs, cipher)
            val original = session()
            store.saveSession(original, store.beginAuthentication())
            val pending = PendingPayment("pending", "KRONIQ", userA)
            store.savePendingPayment(pending)
            val late = store.beginAuthentication()
            store.requireReauthentication()
            assertFalse(store.saveSession(original, late))
            val reopened = EncryptedAuthSessionStore(prefs, cipher)
            assertNull(reopened.snapshot().session)
            assertEquals(pending, reopened.snapshot().pendingPayments[userA])
            reopened.saveSession(session(userB), reopened.beginAuthentication())
            assertNull(reopened.snapshot().pendingPayments[userB])
            reopened.saveSession(original, reopened.beginAuthentication())
            assertEquals(pending, reopened.snapshot().pendingPayments[userA])
        } finally { job.cancelAndJoin() }
    }

    @Test fun freshOrExistingModernRecord_neverAcceptsSavedStateAsLegacyData() = runBlocking {
        val job = SupervisorJob()
        try {
            val prefs = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) {
                temp.newFile("modern.preferences_pb")
            }
            val store = EncryptedAuthSessionStore(prefs, cipher)
            assertFalse(store.importSavedPendingPayment(PendingPayment("stale", "PREMIUM", userA), store.snapshot().generation))
        } finally { job.cancelAndJoin() }
    }

    @Test fun unavailableDecryptionKey_neverDeletesExistingCiphertext() = runBlocking {
        val job = SupervisorJob()
        try {
            val prefs = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) {
                temp.newFile("lost-key.preferences_pb")
            }
            val store = EncryptedAuthSessionStore(prefs, cipher)
            store.saveSession(session(), store.beginAuthentication())
            store.savePendingPayment(PendingPayment("pending", "PREMIUM", userA))
            val before = prefs.data.first()
            val otherKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
            val unreadable = EncryptedAuthSessionStore(prefs, AesGcmSessionCipher { otherKey })
            try { unreadable.snapshot(); fail("Wrong key accepted") } catch (_: java.security.GeneralSecurityException) {}
            assertEquals(before, prefs.data.first())
            assertEquals("pending", store.snapshot().pendingPayments[userA]!!.purchaseId)
        } finally { job.cancelAndJoin() }
    }

    @Test fun failedEncryptedWrite_preservesPreviousWholeRecord() = runBlocking {
        val job = SupervisorJob()
        try {
            val prefs = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) {
                temp.newFile("atomic.preferences_pb")
            }
            var failWrites = false
            val guarded = object : SessionCipher {
                override fun decrypt(ciphertext: ByteArray) = cipher.decrypt(ciphertext)
                override fun encrypt(plaintext: ByteArray): ByteArray {
                    if (failWrites) throw IllegalStateException("Storage unavailable")
                    return cipher.encrypt(plaintext)
                }
            }
            val store = EncryptedAuthSessionStore(prefs, guarded)
            store.saveSession(session(), store.beginAuthentication())
            val ticket = store.beginAuthentication()
            val before = prefs.data.first()
            failWrites = true
            try { store.saveSession(session(userB), ticket); fail("Write should fail") } catch (_: IllegalStateException) {}
            assertEquals(before, prefs.data.first())
            assertEquals(session(), store.snapshot().session)
        } finally { job.cancelAndJoin() }
    }
}
