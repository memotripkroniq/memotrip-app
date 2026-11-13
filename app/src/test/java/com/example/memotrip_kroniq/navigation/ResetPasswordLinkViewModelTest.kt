package com.example.memotrip_kroniq.navigation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResetPasswordLinkViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val token = "123e4567-e89b-42d3-a456-426614174000"
    private val link get() = "https://memotrip-bff-development.up.railway.app/auth/reset-password/open#token=$token"
    @Before fun before() { Dispatchers.setMain(dispatcher) }
    @After fun after() { Dispatchers.resetMain() }

    @Test fun coldAndWarmLinksTakePriorityAndAreConsumedOnce() {
        val model = ResetPasswordLinkViewModel()
        assertTrue(model.mayLeaveSplash())
        model.accept(link)
        assertFalse(model.mayLeaveSplash())
        val first = model.state.value.generation
        model.navigated(first)
        assertFalse(model.state.value.pendingNavigation)
        assertFalse(model.mayLeaveSplash())
        model.accept(link)
        assertTrue(model.state.value.generation > first)
        model.navigated(first) // stale acknowledgement cannot consume a newer intent
        assertTrue(model.state.value.pendingNavigation)
        model.navigated(model.state.value.generation)
        assertFalse(model.state.value.pendingNavigation)
        model.clear()
        assertTrue(model.mayLeaveSplash())
    }

    @Test fun invalidLinkAndProcessRecreationCannotSubmit() = runTest(dispatcher) {
        val model = ResetPasswordLinkViewModel()
        model.accept(link)
        model.accept("invalid")
        var calls = 0
        model.submit("password") { _, _ -> calls++ }
        ResetPasswordLinkViewModel().submit("password") { _, _ -> calls++ }
        advanceUntilIdle()
        assertEquals(0, calls)
        assertFalse(model.state.value.valid)
        assertTrue(model.state.value.pendingNavigation)
    }

    @Test fun legacyResetSubmitsOnceAndClearsCredentialAfterSuccess() = runTest(dispatcher) {
        val model = ResetPasswordLinkViewModel()
        model.accept("memotrip://reset-password?token=$token")
        var calls = 0
        val reset: suspend (String, String) -> Unit = { actualToken, password ->
            assertEquals(token, actualToken)
            assertEquals("new-password", password)
            calls++
        }
        model.submit("new-password", reset)
        model.submit("new-password", reset)
        advanceUntilIdle()
        assertTrue(model.state.value.completed)
        assertFalse(model.state.value.valid)
        model.submit("new-password", reset)
        advanceUntilIdle()
        assertEquals(1, calls)
        assertFalse(model.state.value.toString().contains(token))
        assertFalse(model.state.value.toString().contains("new-password"))
    }

    @Test fun failedResetAllowsRetryWithoutPersistingPasswordOrErrorDetails() = runTest(dispatcher) {
        val model = ResetPasswordLinkViewModel()
        model.accept(link)
        model.submit("first") { _, _ -> error("secret backend details") }
        advanceUntilIdle()
        assertTrue(model.state.value.failed)
        assertTrue(model.state.value.valid)
        model.submit("second") { _, password -> assertEquals("second", password) }
        advanceUntilIdle()
        assertTrue(model.state.value.completed)
    }

    @Test fun replacedOrCancelledRequestCannotCompleteNewFlow() = runTest(dispatcher) {
        val model = ResetPasswordLinkViewModel()
        model.accept(link)
        val response = CompletableDeferred<Unit>()
        model.submit("password") { _, _ -> withContext(NonCancellable) { response.await() } }
        runCurrent()
        model.accept(link)
        response.complete(Unit)
        advanceUntilIdle()
        assertFalse(model.state.value.completed)
        assertFalse(model.state.value.saving)
        assertTrue(model.state.value.valid)
        model.clear()
        var calls = 0
        model.submit("password") { _, _ -> calls++ }
        advanceUntilIdle()
        assertEquals(0, calls)
    }
}
