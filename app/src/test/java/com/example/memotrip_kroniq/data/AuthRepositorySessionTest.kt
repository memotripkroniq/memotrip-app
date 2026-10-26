package com.example.memotrip_kroniq.data

import com.example.memotrip_kroniq.data.datastore.*
import com.example.memotrip_kroniq.data.model.UserMe
import com.example.memotrip_kroniq.data.remote.AuthApi
import com.example.memotrip_kroniq.data.remote.AuthResponse
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy

class AuthRepositorySessionTest {
    private val user = "11111111-1111-4111-8111-111111111111"
    private fun response(ttl: Long = 900) = AuthResponse("access", "opaque-refresh", "Bearer", ttl,
        "2099-01-01T00:00:00.000Z", "22222222-2222-4222-8222-222222222222", user)

    @Test fun emailGoogleAndSignup_allSaveCompleteSessionBeforeAuthenticatedRead() = runBlocking {
        for (method in listOf("login", "google", "signup")) {
            val store = MemoryStore()
            val dto = response(if (method == "google") 900 else 604800)
            val api = Proxy.newProxyInstance(AuthApi::class.java.classLoader, arrayOf(AuthApi::class.java)) { _, call, _ ->
                when (call.name) {
                    "login", "signup", "loginWithGoogle" -> dto
                    "getMe" -> {
                        assertEquals(dto.accessToken, store.value.value.session!!.accessToken)
                        assertEquals(dto.refreshToken, store.value.value.session!!.refreshToken)
                        UserMe(user, "test@example.com", null, isPremium = false, isKroniq = false)
                    }
                    else -> error("Unexpected API call")
                }
            } as AuthApi
            val repo = AuthRepository(api, store)
            when(method) {
                "login" -> repo.login("test@example.com", "password")
                "google" -> { repo.loginWithGoogle("google-id-token"); repo.getMe() }
                else -> repo.signup("test@example.com", "password", null, "EN")
            }
            val saved = store.snapshot().session!!
            assertEquals(dto.refreshToken, saved.refreshToken)
            assertEquals(dto.userId, saved.userId)
            assertEquals(dto.sessionId, saved.sessionId)
            assertEquals(dto.expiresIn, saved.expiresIn)
            assertEquals(dto.refreshTokenExpiresAt, saved.refreshTokenExpiresAt)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test fun bothSignupViewModelPaths_saveSessionWithoutPuttingTokensInUiState() = runBlocking {
        val dispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        try {
            for (google in listOf(false, true)) {
                val store = MemoryStore()
                val api = Proxy.newProxyInstance(AuthApi::class.java.classLoader, arrayOf(AuthApi::class.java)) { _, _, _ -> response() } as AuthApi
                val vm = com.example.memotrip_kroniq.ui.auth.SignupViewModel(AuthRepository(api, store))
                if (google) vm.signupWithGoogle("id-token") else vm.signup("test@example.com", "password", null)
                assertEquals(com.example.memotrip_kroniq.ui.auth.SignupState.Success, vm.state.value)
                assertEquals(response().refreshToken, store.snapshot().session!!.refreshToken)
                assertFalse(vm.state.value.toString().contains("opaque-refresh"))
            }
        } finally { kotlinx.coroutines.Dispatchers.resetMain() }
    }

    @Test fun meResponseFromAnotherUser_isNeverAccepted() = runBlocking {
        val store = MemoryStore()
        val dto = response()
        store.saveSession(dto.toSession(), store.beginAuthentication())
        val api = Proxy.newProxyInstance(AuthApi::class.java.classLoader, arrayOf(AuthApi::class.java)) { _, _, _ ->
            UserMe("other-user", "other@example.com", null, isPremium = true, isKroniq = false)
        } as AuthApi
        try { AuthRepository(api, store).getMe(); fail("Wrong account accepted") } catch (_: IllegalStateException) {}
    }

    @Test fun missingFieldsFromServer_areRejectedRatherThanSavingPartialSession() {
        val dto = Gson().fromJson("{\"accessToken\":\"access\"}", AuthResponse::class.java)
        try { dto.toSession(); fail("Incomplete response accepted") } catch (_: IllegalArgumentException) {}
    }

    @Test fun dtoPreservesSecondsAndRedactsTokensFromDiagnostics() {
        val dto = response()
        val session = dto.toSession(1000)
        assertEquals(901000, session.accessExpiresAtEpochMillis)
        assertTrue(session.hasUsableAccessToken(900999))
        assertFalse(session.hasUsableAccessToken(901000))
        assertFalse(dto.toString().contains("opaque-refresh"))
        assertFalse(session.toString().contains("opaque-refresh"))
    }

    class MemoryStore : AuthSessionStore {
        val value = MutableStateFlow(AuthStorageState())
        override val session = value.map { it.session }
        override val accessToken = session.map { it?.accessToken }
        override suspend fun snapshot() = value.value
        override suspend fun beginAuthentication(): String {
            val id = java.util.UUID.randomUUID().toString()
            value.value = value.value.copy(generation = id)
            return id
        }
        override suspend fun saveSession(session: AuthSession, expectedGeneration: String): Boolean {
            if(value.value.generation != expectedGeneration) return false
            value.value = value.value.copy(session = session, generation = java.util.UUID.randomUUID().toString())
            return true
        }
        override suspend fun clearTokens() { value.value = AuthStorageState() }
        override suspend fun requireReauthentication() {
            value.value = value.value.copy(session = null, generation = java.util.UUID.randomUUID().toString(), legacyPendingImportAllowed = false)
        }
        override suspend fun savePendingPayment(payment: PendingPayment, expectedGeneration: String?): Boolean {
            if(value.value.session?.userId != payment.userId || (expectedGeneration != null && value.value.generation != expectedGeneration)) return false
            value.value = value.value.copy(pendingPayments = value.value.pendingPayments + (payment.userId to payment), legacyPendingImportAllowed = false)
            return true
        }
        override suspend fun importSavedPendingPayment(payment: PendingPayment, expectedGeneration: String): Boolean {
            if (!value.value.legacyPendingImportAllowed || value.value.generation != expectedGeneration ||
                (value.value.session != null && value.value.session!!.userId != payment.userId)) return false
            value.value = value.value.copy(
                pendingPayments = if (value.value.pendingPayments.containsKey(payment.userId)) value.value.pendingPayments
                    else value.value.pendingPayments + (payment.userId to payment),
                legacyPendingImportAllowed = false
            )
            return true
        }
        override suspend fun clearPendingPayment(payment: PendingPayment) {
            if(value.value.pendingPayments[payment.userId] == payment)
                value.value = value.value.copy(pendingPayments = value.value.pendingPayments - payment.userId)
        }
    }
}
