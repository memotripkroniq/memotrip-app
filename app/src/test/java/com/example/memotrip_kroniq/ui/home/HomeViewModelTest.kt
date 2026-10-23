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

    @Test fun timeout_doesNotRetryPostEvenAfterRecreation() = runTest(dispatcher) {
        payments.failure = SocketTimeoutException()
        val vm = model()
        advanceUntilIdle()
        vm.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals(DummyPayState.CREATION_UNKNOWN, vm.uiState.value.dummyPayState)
        vm.startPremiumDummyPay()
        val restored = model(copySavedState())
        advanceUntilIdle()
        restored.startPremiumDummyPay()
        advanceUntilIdle()
        assertEquals(1, payments.requests.size)
        assertEquals(DummyPayState.CREATION_UNKNOWN, restored.uiState.value.dummyPayState)
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

    @Test fun recreationWhileCreating_verifiesWithoutPostingAgain() = runTest(dispatcher) {
        saved["dummy_plan"] = "PREMIUM"
        saved["dummy_owner"] = "user-1"
        val restored = model(copySavedState())
        advanceUntilIdle()
        assertEquals(DummyPayState.CREATION_UNKNOWN, restored.uiState.value.dummyPayState)
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
        val requests = mutableListOf<DummyPaymentPlan>()
        var failure: Exception? = null
        var status = "COMPLETED"
        var statusFailure: Exception? = null
        var statusResponse: DummyPaymentStatusResponse? = null
        val statusRequests = mutableListOf<String>()
        private var lastPlan = DummyPaymentPlan.PREMIUM
        override suspend fun getDummyPaymentStatus(purchaseId: String): DummyPaymentStatusResponse {
            statusRequests += purchaseId
            statusFailure?.let { throw it }
            return statusResponse ?: DummyPaymentStatusResponse(purchaseId, "DUMMY_PAY", status, lastPlan.apiValue)
        }
        override suspend fun createDummyPayment(plan: DummyPaymentPlan): DummyPaymentResponse {
            requests += plan
            lastPlan = plan
            failure?.let { throw it }
            return DummyPaymentResponse("purchase-1", "DUMMY_PAY", "PENDING", plan.apiValue,
                "5.99", "EUR", "https://example.com/pay?token=secret")
        }
    }
    companion object {
        private fun me(premium: Boolean = false, kroniq: Boolean = false) = UserMe(
            id = "user-1", email = "user@example.com", name = "User",
            isPremium = premium, isKroniq = kroniq
        )
    }
}
