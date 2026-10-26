package com.example.memotrip_kroniq.ui.home

import androidx.lifecycle.SavedStateHandle
import com.example.memotrip_kroniq.data.model.UserMe
import com.example.memotrip_kroniq.data.payments.DummyPaymentPlan
import com.example.memotrip_kroniq.data.payments.PaymentApiException
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentStatusResponse
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentResponse
import com.example.memotrip_kroniq.data.remote.dto.TripDto
import com.example.memotrip_kroniq.data.remote.dto.TripLimitsResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val auth = FakeAuth()
    private val payments = FakePayments()
    private val saved = SavedStateHandle()
    private fun model(handle: SavedStateHandle = saved) = HomeViewModel(auth, FakeTrips(), payments, handle)

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun activePremiumClick_doesNotCreateAnotherPaymentRequest() = runTest(dispatcher) {
        auth.me = me(premium = true)
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals(0, payments.requests.size)
    }

    @Test fun activeKroniqClick_doesNotCreateAnotherPaymentRequest() = runTest(dispatcher) {
        auth.me = me(kroniq = true)
        val vm = model()
        advanceUntilIdle()
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        assertEquals(0, payments.requests.size)
    }

    @Test fun paymentReturn_refreshesAuthMeAndLimitsAndConfirmsPremium() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        vm.consumeDummyPayUrl()
        vm.onDummyPayBrowserOpened()
        auth.me = me(premium = true)
        auth.limits = TripLimitsResponse(allowed = true, plan = "PREMIUM", used = 1, limit = 3)
        vm.refreshUserFromPaymentReturnIfNeeded()
        advanceUntilIdle()
        assertEquals(2, auth.meCalls)
        assertEquals(HomePlanTier.PREMIUM, vm.uiState.value.currentPlan)
        assertEquals("PREMIUM", vm.uiState.value.tripLimitPlan)
        assertEquals(DummyPayState.CONFIRMED, vm.uiState.value.dummyPayState)
        assertFalse(vm.uiState.value.isAwaitingDummyPayReturn)
        assertFalse(vm.uiState.value.canReopenDummyPay)
        assertTrue(saved.keys().isEmpty())
        vm.reopenDummyPay()
        advanceUntilIdle()
        assertNull(vm.uiState.value.dummyPayUrlToOpen)
    }

    @Test fun kroniqConfirmation_unlocksThemesAndUnlimitedTrips() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        auth.me = me(kroniq = true)
        auth.limits = TripLimitsResponse(allowed = false, plan = "KRONIQ", used = 5, limit = null)
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(HomePlanTier.KRONIQ, vm.uiState.value.currentPlan)
        assertFalse(vm.uiState.value.isThemesLocked)
        assertTrue(vm.uiState.value.isAddTripEnabled)
        assertNull(vm.uiState.value.tripLimitLimit)
        assertEquals(DummyPayState.CONFIRMED, vm.uiState.value.dummyPayState)
    }

    @Test fun normalResumeWithoutPaymentFlow_doesNotRefreshAuthMe() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.refreshUserFromPaymentReturnIfNeeded()
        advanceUntilIdle()
        assertEquals(1, auth.meCalls)
    }

    // Closing the browser leaves the purchase pending; no new POST is allowed.
    @Test fun pendingAfterReturn_keepsSamePurchaseAndAllowsReopening() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        vm.consumeDummyPayUrl()
        vm.onDummyPayBrowserOpened()
        payments.status = "PENDING"
        vm.refreshUserFromPaymentReturnIfNeeded()
        advanceUntilIdle()
        assertEquals(DummyPayState.WAITING, vm.uiState.value.dummyPayState)
        assertNull(vm.consumeDummyPayResult())
        assertEquals(DummyPayState.WAITING, vm.uiState.value.dummyPayState)
        assertEquals(1, auth.meCalls)
        vm.reopenDummyPay()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.dummyPayUrlToOpen)
        assertEquals(HomePlanTier.FREE, vm.uiState.value.currentPlan)
        assertEquals("purchase-1", vm.uiState.value.dummyPayPurchaseId)
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        assertEquals(1, payments.requests.size)
    }

    @Test fun verificationFailure_preservesAccessAndAllowsSuccessfulRetry() = runTest(dispatcher) {
        auth.me = me(premium = true)
        val vm = model()
        advanceUntilIdle()
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        auth.failMe = true
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
        assertTrue(vm.uiState.value.isAwaitingDummyPayReturn)
        assertEquals(HomePlanTier.PREMIUM, vm.uiState.value.currentPlan)
        auth.failMe = false
        auth.me = me(kroniq = true)
        auth.limits = TripLimitsResponse(allowed = true, plan = "KRONIQ", used = 0, limit = null)
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.CONFIRMED, vm.uiState.value.dummyPayState)
        assertEquals(1, payments.requests.size)
    }

    @Test fun limitsFailure_doesNotConfirmUntilCoordinatedRetrySucceeds() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        auth.me = me(premium = true)
        auth.failLimits = true
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
        auth.failLimits = false
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.CONFIRMED, vm.uiState.value.dummyPayState)
    }

    @Test fun openingFailure_reopensSameUrlWithoutNewPost() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        val url = vm.uiState.value.dummyPayUrlToOpen
        vm.consumeDummyPayUrl()
        vm.onDummyPayOpenFailed()
        assertEquals(DummyPayState.OPEN_ERROR, vm.uiState.value.dummyPayState)
        vm.reopenDummyPay()
        advanceUntilIdle()
        assertEquals(url, vm.uiState.value.dummyPayUrlToOpen)
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals(1, payments.requests.size)
    }

    @Test fun sameFrameDoubleTapAndWaiting_doNotCreateDuplicatePurchases() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        vm.startPremiumDummyPay()
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        assertEquals(listOf(DummyPaymentPlan.PREMIUM), payments.requests)
    }

    @Test fun verificationInFlight_blocksPurchasesAndDuplicateRefresh() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        val gate = CompletableDeferred<UserMe>()
        auth.meGate = gate
        vm.verifyDummyPay()
        runCurrent()
        assertEquals(DummyPayState.VERIFYING, vm.uiState.value.dummyPayState)
        vm.startKroniqDummyPay()
        vm.verifyDummyPay()
        gate.complete(me(premium = true))
        advanceUntilIdle()
        assertEquals(2, auth.meCalls)
        assertEquals(1, payments.requests.size)
    }

    @Test fun timeout_doesNotRetryPostAfterRecreationWhenRecoveryIsUnavailable() = runTest(dispatcher) {
        payments.failure = SocketTimeoutException()
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.CREATION_UNKNOWN, vm.uiState.value.dummyPayState)
        vm.startPremiumDummyPay()
        payments.recoveryFailure = SocketTimeoutException()
        val restored = model(copySavedState())
        advanceUntilIdle()
        restored.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals(1, payments.requests.size)
        assertEquals(DummyPayState.VERIFICATION_ERROR, restored.uiState.value.dummyPayState)
        assertNull(restored.uiState.value.dummyPayPurchaseId)
    }

    @Test fun recreation_restoresMetadataButNeverUrlOrNewPost() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        vm.consumeDummyPayUrl()
        vm.onDummyPayBrowserOpened()
        // Rotation keeps the VM: no new URL event after consumption.
        assertNull(vm.uiState.value.dummyPayUrlToOpen)
        assertTrue(vm.uiState.value.canReopenDummyPay)
        assertEquals(setOf("dummy_plan", "dummy_owner", "dummy_purchase"), saved.keys())
        payments.status = "PENDING"
        val restored = model(copySavedState())
        advanceUntilIdle()
        assertEquals(DummyPayState.WAITING, restored.uiState.value.dummyPayState)
        assertEquals(listOf("purchase-1"), payments.statusRequests)
        assertEquals("purchase-1", restored.uiState.value.dummyPayPurchaseId)
        assertEquals(DummyPaymentPlan.PREMIUM, restored.uiState.value.dummyPayPlan)
        assertNull(restored.uiState.value.dummyPayUrlToOpen)
        assertFalse(restored.uiState.value.canReopenDummyPay)
        assertEquals(1, payments.requests.size)
    }

    @Test fun recreationWhileCreating_recoversWithoutPostingAgain() = runTest(dispatcher) {
        payments.recovered = listOf(pendingItem())
        payments.status = "PENDING"
        saved["dummy_plan"] = "PREMIUM"
        saved["dummy_owner"] = "user-1"
        val restored = model(copySavedState())
        advanceUntilIdle()
        assertEquals(DummyPayState.WAITING, restored.uiState.value.dummyPayState)
        assertEquals("purchase-1", restored.uiState.value.dummyPayPurchaseId)
        assertEquals(1, payments.recoveryCalls)
        assertEquals(0, payments.requests.size)
    }

    @Test fun restoredPurchaseFromAnotherUser_isDiscarded() = runTest(dispatcher) {
        saved["dummy_plan"] = "PREMIUM"
        saved["dummy_owner"] = "other-user"
        saved["dummy_purchase"] = "old-purchase"
        val vm = model()
        advanceUntilIdle()
        assertEquals(DummyPayState.IDLE, vm.uiState.value.dummyPayState)
        assertNull(vm.uiState.value.dummyPayPurchaseId)
        assertTrue(saved.keys().isEmpty())
    }

    @Test fun logout_clearsMetadataAndInMemoryUrl() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        vm.clearDummyPay()
        vm.reopenDummyPay()
        advanceUntilIdle()
        assertNull(vm.uiState.value.dummyPayUrlToOpen)
        assertNull(vm.uiState.value.dummyPayPlan)
        assertFalse(vm.uiState.value.canReopenDummyPay)
        assertTrue(saved.keys().isEmpty())
    }

    @Test fun olderHomeLimits_completeBeforePaymentVerificationAndCannotOverwriteIt() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        val oldRead = CompletableDeferred<TripLimitsResponse>()
        auth.limitsGate = oldRead
        vm.refreshHomeData()
        runCurrent()
        auth.me = me(kroniq = true)
        auth.limits = TripLimitsResponse(allowed = true, plan = "KRONIQ", used = 10, limit = null)
        vm.verifyDummyPay()
        runCurrent()
        oldRead.complete(TripLimitsResponse(allowed = false, plan = "FREE", used = 1, limit = 1))
        advanceUntilIdle()
        assertEquals("KRONIQ", vm.uiState.value.tripLimitPlan)
        assertTrue(vm.uiState.value.isAddTripEnabled)
        assertEquals(DummyPayState.CONFIRMED, vm.uiState.value.dummyPayState)
    }

    @Test fun explicitHttpRejection_preservesRetryPossibility() = runTest(dispatcher) {
        payments.failure = PaymentApiException(401, "Unauthorized", "Expired", Exception())
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.START_ERROR, vm.uiState.value.dummyPayState)
        assertTrue(saved.keys().isEmpty())
        assertFalse(vm.uiState.value.isPurchaseBlocked)
    }

    @Test fun failed_clearsPurchaseAndAllowsOnlyExplicitNewAttempt() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        payments.status = "FAILED"
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.FAILED, vm.uiState.value.dummyPayState)
        assertNull(vm.uiState.value.dummyPayPurchaseId)
        assertNull(vm.uiState.value.dummyPayPlan)
        assertNull(vm.uiState.value.dummyPayUrlToOpen)
        assertFalse(vm.uiState.value.canReopenDummyPay)
        assertFalse(vm.uiState.value.isPurchaseBlocked)
        assertTrue(saved.keys().isEmpty())
        assertEquals(1, payments.requests.size)
        assertEquals(1, auth.meCalls)
        vm.startPremiumDummyPay()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals(2, payments.requests.size)
    }

    @Test fun statusErrors_preserveMetadataAndCanBeRetriedWithoutAnotherPost() = runTest(dispatcher) {
        for (failure in listOf(SocketTimeoutException(),
            PaymentApiException(401, null, "Unauthorized", Exception()),
            PaymentApiException(404, null, "Not found", Exception()),
            PaymentApiException(503, null, "Unavailable", Exception()))) {
            val handle = SavedStateHandle()
            val vm = model(handle)
            advanceUntilIdle()
            vm.startPremiumDummyPay()
            advanceUntilIdle()
            val count = payments.requests.size
            payments.statusFailure = failure
            vm.verifyDummyPay()
            advanceUntilIdle()
            assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
            assertEquals("purchase-1", handle.get<String>("dummy_purchase"))
            assertTrue(vm.uiState.value.isPurchaseBlocked)
            assertTrue(vm.uiState.value.canReopenDummyPay)
            vm.startPremiumDummyPay()
            payments.statusFailure = null
            payments.status = "PENDING"
            vm.verifyDummyPay()
            advanceUntilIdle()
            assertEquals(DummyPayState.WAITING, vm.uiState.value.dummyPayState)
            assertEquals(count, payments.requests.size)
        }
    }

    @Test fun unknownOrMismatchedResponse_neverFailsOrDiscardsPurchase() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        val valid = DummyPaymentStatusResponse("purchase-1", "DUMMY_PAY", "PENDING", "PREMIUM")
        for (response in listOf(valid.copy(status = "NEW_STATUS"), valid.copy(purchaseId = "other"),
            valid.copy(provider = "OTHER"), valid.copy(plan = "KRONIQ"))) {
            payments.statusResponse = response
            vm.verifyDummyPay()
            advanceUntilIdle()
            assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
            assertEquals("purchase-1", vm.uiState.value.dummyPayPurchaseId)
            assertFalse(saved.keys().isEmpty())
        }
        assertEquals(1, payments.requests.size)
    }

    @Test fun completed_usesActualAccountEntitlementsInsteadOfPurchasedPlan() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        // Another account change may have happened since this purchase completed.
        auth.me = me(kroniq = true)
        auth.limits = TripLimitsResponse(allowed = true, plan = "KRONIQ", used = 4, limit = null)
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.CONFIRMED, vm.uiState.value.dummyPayState)
        assertEquals(HomePlanTier.KRONIQ, vm.uiState.value.currentPlan)
        assertFalse(vm.uiState.value.isPremium)
        assertTrue(saved.keys().isEmpty())
    }

    @Test fun restoredCompleted_refreshesAccountAndClearsMetadataWithoutBrowserOrPost() = runTest(dispatcher) {
        saved["dummy_plan"] = "KRONIQ"
        saved["dummy_owner"] = "user-1"
        saved["dummy_purchase"] = "purchase-1"
        payments.statusResponse = DummyPaymentStatusResponse("purchase-1", "DUMMY_PAY", "COMPLETED", "KRONIQ")
        auth.me = me(kroniq = true)
        auth.limits = TripLimitsResponse(allowed = true, plan = "KRONIQ", used = 4, limit = null)
        val vm = model()
        advanceUntilIdle()
        assertEquals(DummyPayState.CONFIRMED, vm.uiState.value.dummyPayState)
        assertEquals(HomePlanTier.KRONIQ, vm.uiState.value.currentPlan)
        assertNull(vm.uiState.value.dummyPayUrlToOpen)
        assertTrue(saved.keys().isEmpty())
        assertEquals(0, payments.requests.size)
    }

    @Test fun restoredFailed_unlocksNewPurchaseWithoutAutomaticPost() = runTest(dispatcher) {
        saved["dummy_plan"] = "PREMIUM"
        saved["dummy_owner"] = "user-1"
        saved["dummy_purchase"] = "purchase-1"
        payments.status = "FAILED"
        val vm = model()
        advanceUntilIdle()
        assertEquals(DummyPayState.FAILED, vm.uiState.value.dummyPayState)
        assertFalse(vm.uiState.value.isPurchaseBlocked)
        assertTrue(saved.keys().isEmpty())
        assertEquals(0, payments.requests.size)
    }

    @Test fun successfulConfirmation_isConsumedOnceWithoutChangingAccessOrRepeatingOnResume() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        auth.me = me(premium = true)
        auth.limits = TripLimitsResponse(allowed = true, plan = "PREMIUM", used = 1, limit = 3)
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.CONFIRMED, vm.consumeDummyPayResult())
        assertNull(vm.consumeDummyPayResult())
        assertEquals(DummyPayState.IDLE, vm.uiState.value.dummyPayState)
        assertEquals(HomePlanTier.PREMIUM, vm.uiState.value.currentPlan)
        assertEquals(3, vm.uiState.value.tripLimitLimit)
        assertTrue(vm.uiState.value.isAddTripEnabled)
        assertTrue(saved.keys().isEmpty())
        val gets = payments.statusRequests.size
        vm.refreshUserFromPaymentReturnIfNeeded()
        advanceUntilIdle()
        assertNull(vm.consumeDummyPayResult())
        assertEquals(gets, payments.statusRequests.size)
        val restored = model(copySavedState())
        advanceUntilIdle()
        assertNull(restored.consumeDummyPayResult())
        assertEquals(HomePlanTier.PREMIUM, restored.uiState.value.currentPlan)
        assertEquals(1, payments.requests.size)
    }

    @Test fun verificationError_remainsActionableAndFailedIsConsumedOnce() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        payments.statusFailure = SocketTimeoutException()
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertNull(vm.consumeDummyPayResult())
        assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
        assertNotNull(vm.uiState.value.dummyPayPurchaseId)
        payments.statusFailure = null
        payments.status = "FAILED"
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.FAILED, vm.consumeDummyPayResult())
        assertNull(vm.consumeDummyPayResult())
        assertEquals(DummyPayState.IDLE, vm.uiState.value.dummyPayState)
        assertFalse(vm.uiState.value.isPurchaseBlocked)
    }

    @Test fun failedResult_isNotReplayedOnResumeOrRecreationAndNewPurchaseRemainsEnabled() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        payments.status = "FAILED"
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.FAILED, vm.consumeDummyPayResult())
        assertNull(vm.consumeDummyPayResult())
        assertEquals(HomePlanTier.FREE, vm.uiState.value.currentPlan)
        assertFalse(vm.uiState.value.isPurchaseBlocked)
        assertTrue(saved.keys().isEmpty())
        assertNull(vm.uiState.value.dummyPayPurchaseId)
        val gets = payments.statusRequests.size
        vm.refreshUserFromPaymentReturnIfNeeded()
        advanceUntilIdle()
        assertNull(vm.consumeDummyPayResult())
        assertEquals(gets, payments.statusRequests.size)
        val restored = model(copySavedState())
        advanceUntilIdle()
        assertNull(restored.consumeDummyPayResult())
        assertFalse(restored.uiState.value.isPurchaseBlocked)
        assertEquals(1, payments.requests.size)
        restored.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals(2, payments.requests.size)
    }

    @Test fun dismissPending_preservesPurchaseUrlAndDuplicateProtectionWithoutRequests() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        val url = vm.uiState.value.dummyPayUrlToOpen
        vm.consumeDummyPayUrl()
        val gets = payments.statusRequests.size
        vm.dismissDummyPayNotice()
        vm.dismissDummyPayNotice()
        assertTrue(vm.uiState.value.isDummyPayNoticeCollapsed)
        assertEquals("purchase-1", vm.uiState.value.dummyPayPurchaseId)
        assertEquals(DummyPaymentPlan.PREMIUM, vm.uiState.value.dummyPayPlan)
        assertEquals("purchase-1", saved.get<String>("dummy_purchase"))
        assertTrue(vm.uiState.value.canReopenDummyPay)
        assertTrue(vm.uiState.value.isPurchaseBlocked)
        assertEquals(gets, payments.statusRequests.size)
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        assertEquals(1, payments.requests.size)
        vm.showDummyPayNotice()
        assertFalse(vm.uiState.value.isDummyPayNoticeDismissed)
        vm.reopenDummyPay()
        advanceUntilIdle()
        assertEquals(url, vm.uiState.value.dummyPayUrlToOpen)
        assertEquals(1, payments.requests.size)
    }

    @Test fun dismissedPending_staysCollapsedDuringResumeVerificationAndProcessRestoration() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        vm.consumeDummyPayUrl()
        vm.onDummyPayBrowserOpened()
        vm.dismissDummyPayNotice()
        payments.status = "PENDING"
        vm.refreshUserFromPaymentReturnIfNeeded()
        assertEquals(DummyPayState.VERIFYING, vm.uiState.value.dummyPayState)
        assertTrue(vm.uiState.value.isDummyPayNoticeCollapsed)
        advanceUntilIdle()
        assertEquals(DummyPayState.WAITING, vm.uiState.value.dummyPayState)
        assertTrue(vm.uiState.value.isDummyPayNoticeCollapsed)
        val restored = model(copySavedState())
        assertTrue(restored.uiState.value.isDummyPayNoticeCollapsed)
        advanceUntilIdle()
        assertTrue(restored.uiState.value.isDummyPayNoticeCollapsed)
        assertEquals("purchase-1", restored.uiState.value.dummyPayPurchaseId)
        assertNull(restored.uiState.value.dummyPayUrlToOpen)
        assertFalse(restored.uiState.value.canReopenDummyPay)
        assertTrue(restored.uiState.value.isPurchaseBlocked)
        restored.showDummyPayNotice()
        assertFalse(restored.uiState.value.isDummyPayNoticeCollapsed)
        restored.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.WAITING, restored.uiState.value.dummyPayState)
        assertEquals(1, payments.requests.size)
    }

    @Test fun hiddenPending_networkErrorRemainsActionableAndSuccessfulRetryDoesNotReshowBanner() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        vm.dismissDummyPayNotice()
        payments.statusFailure = SocketTimeoutException()
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
        assertFalse(vm.uiState.value.isDummyPayNoticeCollapsed)
        assertTrue(vm.uiState.value.isDummyPayNoticeDismissed)
        assertEquals("purchase-1", vm.uiState.value.dummyPayPurchaseId)
        payments.statusFailure = null
        payments.status = "PENDING"
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isDummyPayNoticeCollapsed)
        assertEquals(1, payments.requests.size)
    }

    @Test fun hiddenPending_terminalResultsClearDismissalAndStillProduceOneSnackbar() = runTest(dispatcher) {
        for (status in listOf("FAILED", "COMPLETED")) {
            auth.me = me()
            val handle = SavedStateHandle()
            val vm = model(handle)
            advanceUntilIdle()
            vm.startPremiumDummyPay()
            advanceUntilIdle()
            vm.dismissDummyPayNotice()
            payments.status = status
            if (status == "COMPLETED") auth.me = me(premium = true)
            vm.verifyDummyPay()
            advanceUntilIdle()
            assertFalse(vm.uiState.value.isDummyPayNoticeDismissed)
            assertTrue(handle.keys().isEmpty())
            assertEquals(if (status == "FAILED") DummyPayState.FAILED else DummyPayState.CONFIRMED,
                vm.consumeDummyPayResult())
            assertNull(vm.consumeDummyPayResult())
            assertFalse(vm.uiState.value.isPurchaseBlocked)
        }
    }

    @Test fun logoutClearsPendingDismissalAndNewPurchaseStartsVisible() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        vm.dismissDummyPayNotice()
        vm.clearDummyPay()
        assertFalse(vm.uiState.value.isDummyPayNoticeDismissed)
        assertTrue(saved.keys().isEmpty())
        val next = model(copySavedState())
        advanceUntilIdle()
        next.startPremiumDummyPay()
        advanceUntilIdle()
        assertFalse(next.uiState.value.isDummyPayNoticeDismissed)
        assertEquals(DummyPayState.WAITING, next.uiState.value.dummyPayState)
    }

    @Test fun verificationErrorNotice_isConsumedOnceWithoutClearingPurchaseOrRetry() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        payments.statusFailure = SocketTimeoutException()
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertTrue(vm.consumeDummyPayVerificationNotice())
        assertFalse(vm.consumeDummyPayVerificationNotice())
        assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
        assertEquals("purchase-1", vm.uiState.value.dummyPayPurchaseId)
        assertTrue(vm.uiState.value.isPurchaseBlocked)
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertTrue(vm.consumeDummyPayVerificationNotice())
        payments.statusFailure = null
        payments.status = "PENDING"
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertFalse(vm.consumeDummyPayVerificationNotice())
        assertEquals(DummyPayState.WAITING, vm.uiState.value.dummyPayState)
        assertEquals(1, payments.requests.size)
    }

    @Test fun durablePending_restoresAfterFreshLoginWithoutOpeningBrowserOrPosting() = runTest(dispatcher) {
        val store = paymentStore()
        payments.status = "PENDING"
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        assertEquals("purchase-1", vm.uiState.value.dummyPayPurchaseId)
        assertEquals(DummyPayState.WAITING, vm.uiState.value.dummyPayState)
        assertFalse(vm.uiState.value.canReopenDummyPay)
        assertNull(vm.uiState.value.dummyPayUrlToOpen)
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        assertTrue(payments.requests.isEmpty())
        assertEquals(listOf("purchase-1"), payments.statusRequests)
    }

    @Test fun durablePending_survivesOfflineAccountAndVerificationErrors() = runTest(dispatcher) {
        val store = paymentStore()
        auth.failMe = true
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        assertEquals("purchase-1", vm.uiState.value.dummyPayPurchaseId)
        assertNotNull(store.snapshot().pendingPayments["user-1"])
        payments.statusFailure = PaymentApiException(401, "ACCESS_TOKEN_EXPIRED", "Expired", IllegalStateException())
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
        assertNotNull(store.snapshot().pendingPayments["user-1"])
        assertTrue(payments.requests.isEmpty())
    }

    @Test fun durablePending_otherUserDoesNotSeeOrVerifyPreviousPurchase() = runTest(dispatcher) {
        val store = paymentStore()
        val other = store.snapshot().session!!.copy(userId = "user-2")
        store.saveSession(other, store.beginAuthentication())
        auth.me = me().copy(id = "user-2")
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        assertNull(vm.uiState.value.dummyPayPurchaseId)
        assertTrue(payments.statusRequests.isEmpty())
        assertNotNull(store.snapshot().pendingPayments["user-1"])
    }

    @Test fun durablePending_terminalStatusesClearPersistentMetadata() = runTest(dispatcher) {
        for (status in listOf("FAILED", "COMPLETED")) {
            val store = paymentStore()
            payments.status = status
            val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
            advanceUntilIdle()
            assertTrue(store.snapshot().pendingPayments.isEmpty())
            assertNotNull(vm.consumeDummyPayResult())
            assertNull(vm.consumeDummyPayResult())
        }
    }

    @Test fun newPurchase_persistsMetadataBeforeOfferingUrl() = runTest(dispatcher) {
        val store = paymentStore()
        store.clearPendingPayment(store.snapshot().pendingPayments["user-1"]!!)
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals("purchase-1", store.snapshot().pendingPayments["user-1"]!!.purchaseId)
        assertNotNull(vm.uiState.value.dummyPayUrlToOpen)
    }

    @Test fun legacySavedPayment_isImportedBeforeForcingNewSignIn() = runTest(dispatcher) {
        val store = com.example.memotrip_kroniq.data.AuthRepositorySessionTest.MemoryStore()
        store.value.value = store.value.value.copy(legacyPendingImportAllowed = true)
        val handle = SavedStateHandle(mapOf("dummy_owner" to "user-1", "dummy_purchase" to "purchase-1", "dummy_plan" to "PREMIUM"))
        val vm = HomeViewModel(auth, FakeTrips(), payments, handle, store)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.requiresSignIn)
        assertEquals("purchase-1", store.snapshot().pendingPayments["user-1"]!!.purchaseId)
        assertTrue(payments.statusRequests.isEmpty())
        assertTrue(payments.requests.isEmpty())
    }

    @Test fun logout_newHomeWithOldSavedStateAndCurrentGenerationCannotReimportPurchase() = runTest(dispatcher) {
        val store = paymentStore()
        val originalSession = store.snapshot().session!!
        store.clearTokens()
        val current = store.snapshot().generation
        fun staleHandle() = SavedStateHandle(mapOf(
            "dummy_owner" to "user-1", "dummy_purchase" to "purchase-1", "dummy_plan" to "PREMIUM",
            "dummy_session_generation" to current
        ))
        val loggedOutHome = HomeViewModel(auth, FakeTrips(), payments, staleHandle(), store)
        advanceUntilIdle()
        assertTrue(loggedOutHome.uiState.value.requiresSignIn)
        assertTrue(store.snapshot().pendingPayments.isEmpty())
        assertNull(loggedOutHome.uiState.value.dummyPayPurchaseId)
        store.saveSession(originalSession, store.beginAuthentication())
        val signedInHome = HomeViewModel(auth, FakeTrips(), payments, staleHandle(), store)
        advanceUntilIdle()
        assertNull(signedInHome.uiState.value.dummyPayPurchaseId)
        assertTrue(payments.statusRequests.isEmpty())
        assertTrue(store.snapshot().pendingPayments.isEmpty())
    }

    @Test fun passwordChange_reauthenticationRestoresPendingOnlyForSameUser() = runTest(dispatcher) {
        val store = paymentStore()
        val original = store.snapshot().session!!
        val lateLogin = store.beginAuthentication()
        store.requireReauthentication()
        assertFalse(store.saveSession(original, lateLogin))
        assertNull(store.snapshot().session)
        assertNotNull(store.snapshot().pendingPayments["user-1"])
        store.saveSession(original.copy(userId = "other"), store.beginAuthentication())
        auth.me = me().copy(id = "other")
        val other = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        assertNull(other.uiState.value.dummyPayPurchaseId)
        assertTrue(payments.statusRequests.isEmpty())
        store.saveSession(original, store.beginAuthentication())
        auth.me = me()
        payments.status = "PENDING"
        val same = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        assertEquals("purchase-1", same.uiState.value.dummyPayPurchaseId)
        assertEquals(DummyPayState.WAITING, same.uiState.value.dummyPayState)
        assertEquals(listOf("purchase-1"), payments.statusRequests)
        assertTrue(payments.requests.isEmpty())
    }

    @Test fun completedPurchase_cannotBeRestoredFromStaleSavedState() = runTest(dispatcher) {
        val store = paymentStore()
        val old = SavedStateHandle(mapOf("dummy_owner" to "user-1", "dummy_purchase" to "purchase-1", "dummy_plan" to "PREMIUM"))
        store.clearPendingPayment(store.snapshot().pendingPayments["user-1"]!!)
        val vm = HomeViewModel(auth, FakeTrips(), payments, old, store)
        advanceUntilIdle()
        assertNull(vm.uiState.value.dummyPayPurchaseId)
        assertTrue(payments.statusRequests.isEmpty())
    }

    @Test fun pendingNoticeCanBeHiddenDuringRestoreAndVerificationWithoutLosingPurchase() = runTest(dispatcher) {
        val gate = CompletableDeferred<UserMe>()
        auth.meGate = gate
        payments.status = "PENDING"
        val handle = SavedStateHandle(mapOf("dummy_owner" to "user-1", "dummy_plan" to "PREMIUM", "dummy_purchase" to "purchase-1"))
        val vm = model(handle)
        runCurrent()
        assertEquals(DummyPayState.UNCONFIRMED, vm.uiState.value.dummyPayState)
        vm.dismissDummyPayNotice()
        assertTrue(vm.uiState.value.isDummyPayNoticeCollapsed)
        vm.showDummyPayNotice()
        vm.verifyDummyPay()
        assertEquals(DummyPayState.VERIFYING, vm.uiState.value.dummyPayState)
        vm.dismissDummyPayNotice()
        assertTrue(vm.uiState.value.isDummyPayNoticeCollapsed)
        assertEquals("purchase-1", handle.get<String>("dummy_purchase"))
        gate.complete(me())
        advanceUntilIdle()
        assertEquals(DummyPayState.WAITING, vm.uiState.value.dummyPayState)
        assertTrue(vm.uiState.value.isDummyPayNoticeCollapsed)
        assertTrue(payments.requests.isEmpty())
    }

    @Test fun invalidPaymentUrlDoesNotOfferOrEmitReopenAction() = runTest(dispatcher) {
        payments.paymentUrl = "https://untrusted.example/payment?token=fixture"
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        vm.consumeDummyPayUrl()
        assertFalse(vm.uiState.value.canReopenDummyPay)
        vm.reopenDummyPay()
        advanceUntilIdle()
        assertNull(vm.uiState.value.dummyPayUrlToOpen)
        assertEquals(1, payments.requests.size)
        assertEquals("purchase-1", vm.uiState.value.dummyPayPurchaseId)
    }

    @Test fun continueAfterProcessRestart_recoversUrlForSamePurchaseWithoutCreating() = runTest(dispatcher) {
        val store = paymentStore()
        payments.status = "PENDING"
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        assertNull(vm.uiState.value.dummyPayUrlToOpen)
        vm.continueDummyPay()
        advanceUntilIdle()
        assertEquals(listOf("purchase-1"), payments.continueRequests)
        assertEquals(payments.paymentUrl, vm.claimDummyPayUrl())
        assertNull(vm.claimDummyPayUrl())
        assertTrue(payments.requests.isEmpty())
        assertNotNull(store.snapshot().pendingPayments["user-1"])
    }

    @Test fun cancelConfirmedFailed_clearsDurablePendingAndAllowsExplicitNewPurchase() = runTest(dispatcher) {
        val store = paymentStore()
        payments.status = "PENDING"
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        vm.cancelDummyPay()
        advanceUntilIdle()
        assertEquals(listOf("purchase-1"), payments.cancelRequests)
        assertTrue(store.snapshot().pendingPayments.isEmpty())
        assertEquals(DummyPayState.FAILED, vm.consumeDummyPayResult())
        assertNull(vm.consumeDummyPayResult())
        assertFalse(vm.uiState.value.isPurchaseBlocked)
        assertNull(vm.claimDummyPayUrl())
        assertTrue(payments.requests.isEmpty())
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals(1, payments.requests.size)
    }

    @Test fun cancelNeverRemovesExistingAccountEntitlements() = runTest(dispatcher) {
        auth.me = me(premium = true)
        val vm = model()
        advanceUntilIdle()
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        vm.cancelDummyPay()
        advanceUntilIdle()
        assertEquals(HomePlanTier.PREMIUM, vm.uiState.value.currentPlan)
        assertEquals(1, auth.meCalls)
        assertNull(vm.claimDummyPayUrl())
    }

    @Test fun bothActions_completedResponseRefreshesActualAccountAndLimits() = runTest(dispatcher) {
        for (cancel in listOf(false, true)) {
            val store = paymentStore()
            payments.status = "PENDING"
            val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
            advanceUntilIdle()
            payments.actionResponse = DummyPaymentStatusResponse("purchase-1", "DUMMY_PAY", "COMPLETED", "PREMIUM")
            // The response plan must never override actual backend account permissions.
            auth.me = me(kroniq = true)
            auth.limits = TripLimitsResponse(allowed = true, plan = "KRONIQ", used = 6, limit = null)
            if (cancel) vm.cancelDummyPay() else vm.continueDummyPay()
            advanceUntilIdle()
            assertEquals(HomePlanTier.KRONIQ, vm.uiState.value.currentPlan)
            assertFalse(vm.uiState.value.isThemesLocked)
            assertTrue(vm.uiState.value.isAddTripEnabled)
            assertNull(vm.uiState.value.tripLimitLimit)
            assertEquals(DummyPayState.CONFIRMED, vm.consumeDummyPayResult())
            assertTrue(store.snapshot().pendingPayments.isEmpty())
            assertNull(vm.claimDummyPayUrl())
            assertTrue(payments.requests.isEmpty())
        }
    }

    @Test fun completedAction_refreshFailureKeepsPendingUntilSuccessfulVerify() = runTest(dispatcher) {
        val store = paymentStore()
        payments.status = "PENDING"
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        payments.actionResponse = DummyPaymentStatusResponse("purchase-1", "DUMMY_PAY", "COMPLETED", "PREMIUM")
        auth.failLimits = true
        vm.cancelDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
        assertNotNull(store.snapshot().pendingPayments["user-1"])
        assertNull(vm.consumeDummyPayResult())
        auth.failLimits = false
        payments.status = "COMPLETED"
        auth.me = me(premium = true)
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.CONFIRMED, vm.consumeDummyPayResult())
        assertTrue(store.snapshot().pendingPayments.isEmpty())
    }

    @Test fun bothActions_transportAndHttpErrorsPreservePendingAndAllowExplicitRetry() = runTest(dispatcher) {
        val failures = listOf(SocketTimeoutException(), PaymentApiException(401, null, "Unauthorized", Exception()),
            PaymentApiException(404, null, "Not found", Exception()), PaymentApiException(503, null, "Unavailable", Exception()))
        for (cancel in listOf(false, true)) for (failure in failures) {
            val store = paymentStore()
            payments.status = "PENDING"
            payments.actionFailure = failure
            val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
            advanceUntilIdle()
            val previous = if (cancel) payments.cancelRequests.size else payments.continueRequests.size
            if (cancel) vm.cancelDummyPay() else vm.continueDummyPay()
            advanceUntilIdle()
            assertEquals(previous + 1, if (cancel) payments.cancelRequests.size else payments.continueRequests.size)
            assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
            assertNotNull(store.snapshot().pendingPayments["user-1"])
            assertTrue(vm.uiState.value.isPurchaseBlocked)
            assertNull(vm.claimDummyPayUrl())
            payments.actionFailure = null
            if (cancel) vm.cancelDummyPay() else vm.continueDummyPay()
            advanceUntilIdle()
            assertEquals(if (cancel) DummyPayState.FAILED else DummyPayState.WAITING, vm.uiState.value.dummyPayState)
            assertTrue(payments.requests.isEmpty())
        }
    }

    @Test fun continueMalformedOrForeignResponse_neverOpensOrDeletesPurchase() = runTest(dispatcher) {
        val valid = DummyPaymentStatusResponse("purchase-1", "DUMMY_PAY", "PENDING", "PREMIUM", payments.paymentUrl)
        for (response in listOf(valid.copy(purchaseId = "other"), valid.copy(plan = "KRONIQ"),
            valid.copy(provider = "OTHER"), valid.copy(status = "UNKNOWN"), valid.copy(paymentUrl = null),
            valid.copy(paymentUrl = "http://untrusted.example/payment"),
            valid.copy(paymentUrl = "${com.memotrip_kroniq.BuildConfig.BASE_URL}payments/dummy/other?token=fixture"))) {
            val store = paymentStore()
            payments.status = "PENDING"
            payments.actionResponse = response
            val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
            advanceUntilIdle()
            vm.continueDummyPay()
            advanceUntilIdle()
            assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
            assertNull(vm.claimDummyPayUrl())
            assertNotNull(store.snapshot().pendingPayments["user-1"])
        }
        assertTrue(payments.requests.isEmpty())
    }

    @Test fun cancelPendingResponse_doesNotPretendCancellationSucceeded() = runTest(dispatcher) {
        val store = paymentStore()
        payments.status = "PENDING"
        payments.actionResponse = DummyPaymentStatusResponse("purchase-1", "DUMMY_PAY", "PENDING", "PREMIUM")
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        vm.cancelDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.WAITING, vm.uiState.value.dummyPayState)
        assertTrue(vm.uiState.value.isPurchaseBlocked)
        assertNotNull(store.snapshot().pendingPayments["user-1"])
        assertNull(vm.consumeDummyPayResult())
    }

    @Test fun actionInFlight_blocksContinueCancelVerifyAndPurchaseInSameFrame() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        val gate = CompletableDeferred<DummyPaymentStatusResponse>()
        payments.actionGate = gate
        vm.continueDummyPay()
        vm.continueDummyPay()
        vm.cancelDummyPay()
        vm.verifyDummyPay()
        vm.startKroniqDummyPay()
        runCurrent()
        assertEquals(DummyPayState.VERIFYING, vm.uiState.value.dummyPayState)
        assertNull(vm.claimDummyPayUrl())
        assertEquals(1, payments.continueRequests.size)
        assertTrue(payments.cancelRequests.isEmpty())
        assertTrue(payments.statusRequests.isEmpty())
        gate.complete(DummyPaymentStatusResponse("purchase-1", "DUMMY_PAY", "PENDING", "PREMIUM", payments.paymentUrl))
        advanceUntilIdle()
        assertEquals(1, payments.requests.size)
        vm.cancelDummyPay()
        // Even a previously queued URL must be invalidated before Cancel responds.
        assertNull(vm.claimDummyPayUrl())
        advanceUntilIdle()
    }

    @Test fun lateContinueAfterAccountSwitchOrLogout_neverOpensUrlOrChangesOtherAccount() = runTest(dispatcher) {
        for (logout in listOf(false, true)) {
            val store = paymentStore()
            payments.status = "PENDING"
            val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
            advanceUntilIdle()
            val gate = CompletableDeferred<DummyPaymentStatusResponse>()
            payments.actionGate = gate
            vm.continueDummyPay()
            runCurrent()
            if (logout) store.clearTokens() else {
                val session = store.snapshot().session!!.copy(userId = "other")
                store.saveSession(session, store.beginAuthentication())
            }
            gate.complete(DummyPaymentStatusResponse("purchase-1", "DUMMY_PAY", "PENDING", "PREMIUM", payments.paymentUrl))
            advanceUntilIdle()
            assertNull(vm.uiState.value.dummyPayUrlToOpen)
            assertNull(vm.claimDummyPayUrl())
            if (logout) assertTrue(store.snapshot().pendingPayments.isEmpty())
            else assertNotNull(store.snapshot().pendingPayments["user-1"])
            payments.actionGate = null
        }
    }

    @Test fun queuedUrlCannotOpenAfterSessionGenerationChanges() = runTest(dispatcher) {
        val store = paymentStore()
        payments.status = "PENDING"
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        vm.continueDummyPay()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.dummyPayUrlToOpen)
        store.requireReauthentication()
        assertNull(vm.claimDummyPayUrl())
        assertNotNull(store.snapshot().pendingPayments["user-1"])
    }

    @Test fun continueAlreadyFailed_clearsPendingWithoutOpeningBrowser() = runTest(dispatcher) {
        val store = paymentStore()
        payments.status = "PENDING"
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        payments.actionResponse = DummyPaymentStatusResponse("purchase-1", "DUMMY_PAY", "FAILED", "PREMIUM")
        vm.continueDummyPay()
        advanceUntilIdle()
        assertTrue(store.snapshot().pendingPayments.isEmpty())
        assertEquals(DummyPayState.FAILED, vm.consumeDummyPayResult())
        assertNull(vm.claimDummyPayUrl())
        assertTrue(payments.requests.isEmpty())
    }

    @Test fun lateCancelCompletedAfterAccountChange_doesNotRefreshOrClearOldOwnerMetadata() = runTest(dispatcher) {
        val store = paymentStore()
        payments.status = "PENDING"
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        val meCalls = auth.meCalls
        val gate = CompletableDeferred<DummyPaymentStatusResponse>()
        payments.actionGate = gate
        vm.cancelDummyPay()
        runCurrent()
        val session = store.snapshot().session!!.copy(userId = "other")
        store.saveSession(session, store.beginAuthentication())
        gate.complete(DummyPaymentStatusResponse("purchase-1", "DUMMY_PAY", "COMPLETED", "PREMIUM"))
        advanceUntilIdle()
        assertEquals(meCalls, auth.meCalls)
        assertNotNull(store.snapshot().pendingPayments["user-1"])
        assertNull(vm.consumeDummyPayResult())
        assertNull(vm.claimDummyPayUrl())
        assertNull(vm.uiState.value.dummyPayPurchaseId)
    }

    @Test fun accountChangeDuringCompletedRefresh_preventsStaleEntitlementsAndPendingDeletion() = runTest(dispatcher) {
        val store = paymentStore()
        payments.status = "PENDING"
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        val gate = CompletableDeferred<TripLimitsResponse>()
        auth.limitsGate = gate
        auth.me = me(premium = true)
        payments.actionResponse = DummyPaymentStatusResponse("purchase-1", "DUMMY_PAY", "COMPLETED", "PREMIUM")
        vm.continueDummyPay()
        runCurrent()
        val session = store.snapshot().session!!.copy(userId = "other")
        store.saveSession(session, store.beginAuthentication())
        gate.complete(TripLimitsResponse(allowed = true, plan = "PREMIUM", used = 0, limit = 3))
        advanceUntilIdle()
        assertEquals(HomePlanTier.FREE, vm.uiState.value.currentPlan)
        assertNotNull(store.snapshot().pendingPayments["user-1"])
        assertNull(vm.consumeDummyPayResult())
        assertNull(vm.claimDummyPayUrl())
    }

    private fun pendingItem(id: String = "purchase-1", plan: String = "PREMIUM") =
        com.example.memotrip_kroniq.data.remote.dto.PendingDummyPaymentItem(
            id, "DUMMY_PAY", "PENDING", plan, "2026-10-26T10:00:00.000Z")

    @Test fun failedPersistence_keepsKnownIdAndVerifiesWithoutCreatingAgain() = runTest(dispatcher) {
        val base = paymentStore()
        base.clearPendingPayment(base.snapshot().pendingPayments["user-1"]!!)
        val store = FaultStore(base)
        val vm = HomeViewModel(auth, FakeTrips(), payments, saved, store)
        advanceUntilIdle()
        store.failSave = true
        payments.status = "PENDING"
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
        assertEquals("purchase-1", vm.uiState.value.dummyPayPurchaseId)
        assertTrue(base.snapshot().pendingPayments.isEmpty())
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.WAITING, vm.uiState.value.dummyPayState)
        assertEquals("purchase-1", saved.get<String>("dummy_purchase"))
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        assertEquals(1, payments.requests.size)
        store.failSave = false
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals("purchase-1", base.snapshot().pendingPayments["user-1"]!!.purchaseId)
    }

    @Test fun restartWithoutSavedState_recoversIdAfterFailedPersistenceWithoutPostOrBrowser() = runTest(dispatcher) {
        val base = paymentStore()
        base.clearPendingPayment(base.snapshot().pendingPayments["user-1"]!!)
        val store = FaultStore(base)
        val original = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        store.failSave = true
        original.startPremiumDummyPay()
        advanceUntilIdle()
        assertTrue(base.snapshot().pendingPayments.isEmpty())
        payments.recovered = listOf(pendingItem())
        payments.status = "PENDING"
        val restored = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        assertEquals("purchase-1", restored.uiState.value.dummyPayPurchaseId)
        assertEquals(DummyPayState.WAITING, restored.uiState.value.dummyPayState)
        assertNull(restored.claimDummyPayUrl())
        assertTrue(restored.uiState.value.isPurchaseBlocked)
        assertEquals(1, payments.requests.size)
    }

    @Test fun unknownCreate_manualRecoveryFindsExistingPurchaseAndNeverRepeatsPost() = runTest(dispatcher) {
        val base = paymentStore()
        base.clearPendingPayment(base.snapshot().pendingPayments["user-1"]!!)
        val vm = HomeViewModel(auth, FakeTrips(), payments, saved, base)
        advanceUntilIdle()
        payments.failure = SocketTimeoutException()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.CREATION_UNKNOWN, vm.uiState.value.dummyPayState)
        payments.recovered = listOf(pendingItem())
        payments.status = "PENDING"
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals("purchase-1", vm.uiState.value.dummyPayPurchaseId)
        assertEquals(DummyPayState.WAITING, vm.uiState.value.dummyPayState)
        assertEquals(1, payments.requests.size)
    }

    @Test fun pendingConflict_recoversActualPlanWithoutAutomaticCreateRetry() = runTest(dispatcher) {
        val base = paymentStore()
        base.clearPendingPayment(base.snapshot().pendingPayments["user-1"]!!)
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), base)
        advanceUntilIdle()
        payments.failure = PaymentApiException(409, "DUMMY_PAY_PENDING_EXISTS", "Conflict", Exception())
        payments.recovered = listOf(pendingItem())
        payments.status = "PENDING"
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPaymentPlan.PREMIUM, vm.uiState.value.dummyPayPlan)
        assertEquals("purchase-1", vm.uiState.value.dummyPayPurchaseId)
        assertEquals(DummyPayState.WAITING, vm.uiState.value.dummyPayState)
        assertEquals(1, payments.requests.size)
        assertEquals(2, payments.recoveryCalls)
    }

    @Test fun multiplePending_areProcessedOneAtATimeAndBlockPurchasesUntilAllResolved() = runTest(dispatcher) {
        val base = paymentStore()
        payments.recovered = listOf(pendingItem(), pendingItem("purchase-2", "KRONIQ"))
        payments.status = "PENDING"
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), base)
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.dummyPayPendingCount)
        vm.cancelDummyPay()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isPurchaseBlocked)
        assertEquals(DummyPayState.FAILED, vm.consumeDummyPayResult())
        advanceUntilIdle()
        assertEquals("purchase-2", vm.uiState.value.dummyPayPurchaseId)
        assertEquals(DummyPaymentPlan.KRONIQ, vm.uiState.value.dummyPayPlan)
        assertEquals(1, vm.uiState.value.dummyPayPendingCount)
        assertTrue(vm.uiState.value.isPurchaseBlocked)
        vm.startPremiumDummyPay()
        vm.cancelDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.FAILED, vm.consumeDummyPayResult())
        assertFalse(vm.uiState.value.isPurchaseBlocked)
        assertTrue(payments.requests.isEmpty())
        assertEquals(listOf("purchase-1", "purchase-2"), payments.cancelRequests)
        assertTrue(base.snapshot().pendingPayments.isEmpty())
    }

    @Test fun recoveryErrors_preserveSessionAndKnownIdAndBlockNewPurchasesUntilRetry() = runTest(dispatcher) {
        for (failure in listOf(SocketTimeoutException(), PaymentApiException(401, null, "Unauthorized", Exception()))) {
            val base = paymentStore()
            payments.recoveryFailure = failure
            val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), base)
            advanceUntilIdle()
            assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
            assertEquals("purchase-1", vm.uiState.value.dummyPayPurchaseId)
            assertFalse(vm.uiState.value.isDummyPayRecoveryComplete)
            assertNotNull(base.snapshot().session)
            assertNotNull(base.snapshot().pendingPayments["user-1"])
            vm.startKroniqDummyPay()
            assertTrue(payments.requests.isEmpty())
            payments.recoveryFailure = null
            payments.status = "PENDING"
            vm.verifyDummyPay()
            advanceUntilIdle()
            assertEquals(DummyPayState.WAITING, vm.uiState.value.dummyPayState)
            assertTrue(vm.uiState.value.isDummyPayRecoveryComplete)
        }
    }

    @Test fun lateRecoveryAfterAccountChange_cannotPersistOrDisplayForeignPurchases() = runTest(dispatcher) {
        val base = paymentStore()
        val gate = CompletableDeferred<List<com.example.memotrip_kroniq.data.remote.dto.PendingDummyPaymentItem>>()
        payments.recoveryGate = gate
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), base)
        runCurrent()
        assertTrue(vm.uiState.value.isRecoveringDummyPay)
        val session = base.snapshot().session!!.copy(userId = "other")
        base.saveSession(session, base.beginAuthentication())
        gate.complete(listOf(pendingItem("foreign")))
        advanceUntilIdle()
        assertNull(vm.uiState.value.dummyPayPurchaseId)
        assertNull(vm.claimDummyPayUrl())
        assertEquals("purchase-1", base.snapshot().pendingPayments["user-1"]!!.purchaseId)
        assertNull(base.snapshot().pendingPayments["other"])
    }

    @Test fun terminalCleanupFollowedByReadFailure_isRetryableForFailedAndCompleted() = runTest(dispatcher) {
        for (status in listOf("FAILED", "COMPLETED")) {
            val base = paymentStore()
            val store = FaultStore(base)
            payments.status = "PENDING"
            val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
            advanceUntilIdle()
            payments.actionResponse = DummyPaymentStatusResponse("purchase-1", "DUMMY_PAY", status, "PREMIUM")
            store.readFailuresAfterClear = 1
            if (status == "COMPLETED") auth.me = me(premium = true)
            vm.cancelDummyPay()
            advanceUntilIdle()
            assertTrue(base.snapshot().pendingPayments.isEmpty())
            assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
            assertTrue(vm.uiState.value.isPurchaseBlocked)
            assertNull(vm.consumeDummyPayResult())
            payments.status = status
            vm.verifyDummyPay()
            advanceUntilIdle()
            assertEquals(if (status == "FAILED") DummyPayState.FAILED else DummyPayState.CONFIRMED,
                vm.consumeDummyPayResult())
            assertFalse(vm.uiState.value.isPurchaseBlocked)
            assertTrue(payments.requests.isEmpty())
        }
    }

    @Test fun restartAfterTerminalCleanupReadFailure_usesBackendAccountWithoutStaleSavedImport() = runTest(dispatcher) {
        val base = paymentStore()
        val store = FaultStore(base)
        payments.status = "PENDING"
        val vm = HomeViewModel(auth, FakeTrips(), payments, saved, store)
        advanceUntilIdle()
        store.readFailuresAfterClear = 1
        vm.cancelDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
        val restored = HomeViewModel(auth, FakeTrips(), payments, copySavedState(), store)
        advanceUntilIdle()
        assertNull(restored.uiState.value.dummyPayPurchaseId)
        assertEquals(DummyPayState.IDLE, restored.uiState.value.dummyPayState)
        assertFalse(restored.uiState.value.isPurchaseBlocked)
        assertTrue(payments.requests.isEmpty())
    }

    @Test fun failedTerminalWrite_retainsMetadataAndAllowsExplicitRetry() = runTest(dispatcher) {
        val base = paymentStore()
        val store = FaultStore(base)
        payments.status = "PENDING"
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), store)
        advanceUntilIdle()
        store.failClear = true
        vm.cancelDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
        assertNotNull(base.snapshot().pendingPayments["user-1"])
        store.failClear = false
        payments.status = "FAILED"
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.FAILED, vm.consumeDummyPayResult())
    }

    @Test fun emptyRecoveryAfterAmbiguousCreate_doesNotAutomaticallyPostAgain() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        payments.failure = SocketTimeoutException()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(1, payments.recoveryCalls)
        assertEquals(1, payments.requests.size)
        assertEquals(DummyPayState.IDLE, vm.uiState.value.dummyPayState)
        assertTrue(vm.uiState.value.isDummyPayRecoveryComplete)
    }

    @Test fun restartBetweenHistoricalPayments_recoversRemainingPlanAndContinueUsesSameId() = runTest(dispatcher) {
        val base = paymentStore()
        payments.status = "PENDING"
        payments.recovered = listOf(pendingItem(), pendingItem("purchase-2", "KRONIQ"))
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), base)
        advanceUntilIdle()
        vm.cancelDummyPay()
        advanceUntilIdle()
        vm.consumeDummyPayResult()
        advanceUntilIdle()
        payments.recovered = listOf(pendingItem("purchase-2", "KRONIQ"))
        val restored = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), base)
        advanceUntilIdle()
        assertEquals("purchase-2", restored.uiState.value.dummyPayPurchaseId)
        assertEquals(1, restored.uiState.value.dummyPayPendingCount)
        payments.paymentUrl = "${com.memotrip_kroniq.BuildConfig.BASE_URL}payments/dummy/purchase-2?token=fixture"
        restored.continueDummyPay()
        advanceUntilIdle()
        assertEquals(listOf("purchase-2"), payments.continueRequests)
        assertNotNull(restored.claimDummyPayUrl())
        assertTrue(payments.requests.isEmpty())
    }

    @Test fun recoveryWithoutLocalId_remainsBlockedOn401AndCanExplicitlyRetry() = runTest(dispatcher) {
        val base = paymentStore()
        base.clearPendingPayment(base.snapshot().pendingPayments["user-1"]!!)
        payments.recoveryFailure = PaymentApiException(401, null, "Unauthorized", Exception())
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), base)
        advanceUntilIdle()
        assertNull(vm.uiState.value.dummyPayPlan)
        assertFalse(vm.uiState.value.isDummyPayRecoveryComplete)
        assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
        vm.startPremiumDummyPay()
        assertTrue(payments.requests.isEmpty())
        assertNotNull(base.snapshot().session)
        payments.recoveryFailure = null
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.IDLE, vm.uiState.value.dummyPayState)
        assertFalse(vm.uiState.value.isPurchaseBlocked)
        assertTrue(payments.requests.isEmpty())
    }

    @Test fun lateRecoveryAfterLogout_neverResurrectsPendingOrPosts() = runTest(dispatcher) {
        val base = paymentStore()
        val gate = CompletableDeferred<List<com.example.memotrip_kroniq.data.remote.dto.PendingDummyPaymentItem>>()
        payments.recoveryGate = gate
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), base)
        runCurrent()
        vm.verifyDummyPay()
        vm.startPremiumDummyPay()
        assertEquals(1, payments.recoveryCalls)
        base.clearTokens()
        gate.complete(listOf(pendingItem()))
        advanceUntilIdle()
        assertNull(vm.uiState.value.dummyPayPurchaseId)
        assertNull(vm.claimDummyPayUrl())
        assertTrue(base.snapshot().pendingPayments.isEmpty())
        assertTrue(payments.requests.isEmpty())
    }

    @Test fun terminalResultAfterIncompleteRecovery_retriesListWithoutLosingRecoveryAction() = runTest(dispatcher) {
        val base = paymentStore()
        payments.recoveryFailure = SocketTimeoutException()
        val vm = HomeViewModel(auth, FakeTrips(), payments, SavedStateHandle(), base)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isDummyPayRecoveryComplete)
        vm.cancelDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.FAILED, vm.consumeDummyPayResult())
        advanceUntilIdle()
        assertEquals(DummyPayState.VERIFICATION_ERROR, vm.uiState.value.dummyPayState)
        assertTrue(vm.uiState.value.isPurchaseBlocked)
        assertNull(vm.uiState.value.dummyPayPurchaseId)
        payments.recoveryFailure = null
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isPurchaseBlocked)
        assertTrue(payments.requests.isEmpty())
    }

    @Test fun emptyRecoveryAfterUnknownCreate_refreshesCompletedAccountAndTripLimits() = runTest(dispatcher) {
        val vm = model()
        advanceUntilIdle()
        payments.failure = SocketTimeoutException()
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        auth.me = me(kroniq = true)
        auth.limits = TripLimitsResponse(allowed = true, plan = "KRONIQ", used = 7, limit = null)
        vm.verifyDummyPay()
        advanceUntilIdle()
        assertEquals(HomePlanTier.KRONIQ, vm.uiState.value.currentPlan)
        assertFalse(vm.uiState.value.isThemesLocked)
        assertTrue(vm.uiState.value.isAddTripEnabled)
        assertNull(vm.uiState.value.tripLimitLimit)
        assertTrue(vm.uiState.value.isDummyPayRecoveryComplete)
        assertEquals(1, payments.requests.size)
        vm.startKroniqDummyPay()
        advanceUntilIdle()
        assertEquals(1, payments.requests.size)
    }

    private class FaultStore(private val delegate: com.example.memotrip_kroniq.data.AuthRepositorySessionTest.MemoryStore) :
        com.example.memotrip_kroniq.data.datastore.AuthSessionStore by delegate {
        var failSave = false
        var failClear = false
        var readFailuresAfterClear = 0
        private var readFailures = 0
        override suspend fun snapshot(): com.example.memotrip_kroniq.data.datastore.AuthStorageState {
            if (readFailures > 0) { readFailures--; throw java.io.IOException("Fixture read failure") }
            return delegate.snapshot()
        }
        override suspend fun savePendingPayment(payment: com.example.memotrip_kroniq.data.datastore.PendingPayment, expectedGeneration: String?): Boolean {
            if (failSave) throw java.io.IOException("Fixture write failure")
            return delegate.savePendingPayment(payment, expectedGeneration)
        }
        override suspend fun clearPendingPayment(payment: com.example.memotrip_kroniq.data.datastore.PendingPayment) {
            if (failClear) throw java.io.IOException("Fixture cleanup failure")
            delegate.clearPendingPayment(payment)
            readFailures = readFailuresAfterClear
            readFailuresAfterClear = 0
        }
    }

    private suspend fun paymentStore(): com.example.memotrip_kroniq.data.AuthRepositorySessionTest.MemoryStore {
        val store = com.example.memotrip_kroniq.data.AuthRepositorySessionTest.MemoryStore()
        store.saveSession(com.example.memotrip_kroniq.data.datastore.AuthSession(
            "access", "refresh", "Bearer", 900, "2099-01-01T00:00:00.000Z", "session", "user-1", System.currentTimeMillis()
        ), store.beginAuthentication())
        store.savePendingPayment(com.example.memotrip_kroniq.data.datastore.PendingPayment("purchase-1", "PREMIUM", "user-1"))
        return store
    }

    private fun copySavedState() = SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) })

    private class FakeAuth : HomeAuthDataSource {
        var me = me()
        var limits = TripLimitsResponse(allowed = true, plan = "FREE", used = 0, limit = 1)
        var failMe = false
        var failLimits = false
        var meCalls = 0
        var meGate: CompletableDeferred<UserMe>? = null
        var limitsGate: CompletableDeferred<TripLimitsResponse>? = null
        override suspend fun getMe(): UserMe {
            meCalls++
            if (failMe) throw IllegalStateException("Offline")
            val gate = meGate
            meGate = null
            return gate?.await() ?: me
        }
        override suspend fun getTripLimits(): TripLimitsResponse {
            if (failLimits) throw IllegalStateException("Offline")
            val gate = limitsGate
            limitsGate = null
            return gate?.await() ?: limits
        }
    }
    private class FakeTrips : HomeTripsDataSource {
        override suspend fun getMyTrips(): List<TripDto> = emptyList()
    }
    private class FakePayments : HomePaymentsDataSource {
        var recovered = emptyList<com.example.memotrip_kroniq.data.remote.dto.PendingDummyPaymentItem>()
        var recoveryFailure: Exception? = null
        var recoveryGate: CompletableDeferred<List<com.example.memotrip_kroniq.data.remote.dto.PendingDummyPaymentItem>>? = null
        var recoveryCalls = 0
        override suspend fun getAllPendingDummyPayments(): List<com.example.memotrip_kroniq.data.remote.dto.PendingDummyPaymentItem> {
            recoveryCalls++
            recoveryFailure?.let { throw it }
            return recoveryGate?.await() ?: recovered
        }

        val requests = mutableListOf<DummyPaymentPlan>()
        var failure: Exception? = null
        var status = "COMPLETED"
        var paymentUrl = "${com.memotrip_kroniq.BuildConfig.BASE_URL}payments/dummy/purchase-1?token=fixture"
        var statusFailure: Exception? = null
        var statusResponse: DummyPaymentStatusResponse? = null
        val statusRequests = mutableListOf<String>()
        private var lastPlan = DummyPaymentPlan.PREMIUM
        val continueRequests = mutableListOf<String>()
        val cancelRequests = mutableListOf<String>()
        var actionFailure: Exception? = null
        var actionGate: CompletableDeferred<DummyPaymentStatusResponse>? = null
        var actionResponse: DummyPaymentStatusResponse? = null
        override suspend fun continueDummyPayment(purchaseId: String): DummyPaymentStatusResponse {
            continueRequests += purchaseId
            actionFailure?.let { throw it }
            return actionGate?.await() ?: actionResponse ?: DummyPaymentStatusResponse(
                purchaseId, "DUMMY_PAY", "PENDING", responsePlan(purchaseId), paymentUrl)
        }
        override suspend fun cancelDummyPayment(purchaseId: String): DummyPaymentStatusResponse {
            cancelRequests += purchaseId
            actionFailure?.let { throw it }
            return actionGate?.await() ?: actionResponse ?: DummyPaymentStatusResponse(
                purchaseId, "DUMMY_PAY", "FAILED", responsePlan(purchaseId))
        }
        private fun responsePlan(purchaseId: String) = recovered.firstOrNull { it.purchaseId == purchaseId }?.plan ?: lastPlan.apiValue
        override suspend fun getDummyPaymentStatus(purchaseId: String): DummyPaymentStatusResponse {
            statusRequests += purchaseId
            statusFailure?.let { throw it }
            return statusResponse ?: DummyPaymentStatusResponse(purchaseId, "DUMMY_PAY", status, responsePlan(purchaseId))
        }
        override suspend fun createDummyPayment(plan: DummyPaymentPlan): DummyPaymentResponse {
            requests += plan
            lastPlan = plan
            failure?.let { throw it }
            return DummyPaymentResponse("purchase-1", "DUMMY_PAY", "PENDING", plan.apiValue,
                "5.99", "EUR", paymentUrl)
        }
    }
    companion object {
        private fun me(premium: Boolean = false, kroniq: Boolean = false) = UserMe(
            id = "user-1", email = "user@example.com", name = "User",
            isPremium = premium, isKroniq = kroniq
        )
    }
}
