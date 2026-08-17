package com.example.memotrip_kroniq.ui.home

import com.example.memotrip_kroniq.data.model.UserMe
import com.example.memotrip_kroniq.data.payments.DummyPaymentPlan
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentResponse
import com.example.memotrip_kroniq.data.remote.dto.TripDto
import com.example.memotrip_kroniq.data.remote.dto.TripLimitsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun activePremiumClick_doesNotCreateAnotherPaymentRequest() = runTest(dispatcher) {
        val auth = FakeHomeAuthDataSource(
            meQueue = ArrayDeque(
                listOf(
                    userMe(isPremium = true, isKroniq = false)
                )
            )
        )
        val payments = FakeHomePaymentsDataSource()
        val viewModel = HomeViewModel(
            authRepository = auth,
            tripsRepository = FakeHomeTripsDataSource(),
            paymentsRepository = payments
        )

        advanceUntilIdle()
        viewModel.startPremiumDummyPay()
        advanceUntilIdle()

        assertEquals(0, payments.requests.size)
    }

    @Test
    fun activeKroniqClick_doesNotCreateAnotherPaymentRequest() = runTest(dispatcher) {
        val auth = FakeHomeAuthDataSource(
            meQueue = ArrayDeque(
                listOf(
                    userMe(isPremium = false, isKroniq = true)
                )
            )
        )
        val payments = FakeHomePaymentsDataSource()
        val viewModel = HomeViewModel(
            authRepository = auth,
            tripsRepository = FakeHomeTripsDataSource(),
            paymentsRepository = payments
        )

        advanceUntilIdle()
        viewModel.startKroniqDummyPay()
        advanceUntilIdle()

        assertEquals(0, payments.requests.size)
    }

    @Test
    fun paymentReturn_refreshesAuthMeAndUpdatesPlan() = runTest(dispatcher) {
        val auth = FakeHomeAuthDataSource(
            meQueue = ArrayDeque(
                listOf(
                    userMe(isPremium = false, isKroniq = false),
                    userMe(isPremium = true, isKroniq = false)
                )
            )
        )
        val viewModel = HomeViewModel(
            authRepository = auth,
            tripsRepository = FakeHomeTripsDataSource(),
            paymentsRepository = FakeHomePaymentsDataSource()
        )

        advanceUntilIdle()
        assertEquals(HomePlanTier.FREE, viewModel.uiState.value.currentPlan)

        viewModel.onDummyPayBrowserOpened()
        viewModel.refreshUserFromPaymentReturnIfNeeded()
        advanceUntilIdle()

        assertEquals(2, auth.getMeCalls)
        assertEquals(HomePlanTier.PREMIUM, viewModel.uiState.value.currentPlan)
        assertFalse(viewModel.uiState.value.isAwaitingDummyPayReturn)
    }

    @Test
    fun normalResumeWithoutPaymentFlow_doesNotRefreshAuthMe() = runTest(dispatcher) {
        val auth = FakeHomeAuthDataSource(
            meQueue = ArrayDeque(
                listOf(
                    userMe(isPremium = false, isKroniq = false)
                )
            )
        )
        val viewModel = HomeViewModel(
            authRepository = auth,
            tripsRepository = FakeHomeTripsDataSource(),
            paymentsRepository = FakeHomePaymentsDataSource()
        )

        advanceUntilIdle()
        viewModel.refreshUserFromPaymentReturnIfNeeded()
        advanceUntilIdle()

        assertEquals(1, auth.getMeCalls)
        assertEquals(HomePlanTier.FREE, viewModel.uiState.value.currentPlan)
    }

    @Test
    fun paymentReturnFlagResetsWhenRefreshFails_andKeepsLastKnownState() = runTest(dispatcher) {
        val auth = FakeHomeAuthDataSource(
            meQueue = ArrayDeque(listOf(userMe(isPremium = false, isKroniq = false))),
            throwOnSecondGetMe = true
        )
        val viewModel = HomeViewModel(
            authRepository = auth,
            tripsRepository = FakeHomeTripsDataSource(),
            paymentsRepository = FakeHomePaymentsDataSource()
        )

        advanceUntilIdle()
        viewModel.onDummyPayBrowserOpened()
        viewModel.refreshUserFromPaymentReturnIfNeeded()
        advanceUntilIdle()

        assertEquals(HomePlanTier.FREE, viewModel.uiState.value.currentPlan)
        assertFalse(viewModel.uiState.value.isAwaitingDummyPayReturn)
        assertTrue(viewModel.uiState.value.dummyPayErrorMessage != null)
    }

    private fun userMe(
        isPremium: Boolean,
        isKroniq: Boolean
    ) = UserMe(
        id = "user-1",
        email = "user@example.com",
        name = "User",
        isPremium = isPremium,
        isKroniq = isKroniq
    )

    private class FakeHomeAuthDataSource(
        val meQueue: ArrayDeque<UserMe>,
        private val tripLimits: TripLimitsResponse = TripLimitsResponse(
            allowed = true,
            plan = "FREE",
            used = 0,
            limit = 1
        ),
        private val throwOnSecondGetMe: Boolean = false
    ) : HomeAuthDataSource {
        var getMeCalls: Int = 0
        private var lastKnownMe: UserMe = meQueue.first()

        override suspend fun getMe(): UserMe {
            getMeCalls += 1
            if (throwOnSecondGetMe && getMeCalls >= 2) {
                throw IllegalStateException("refresh failed")
            }
            val next = meQueue.removeFirstOrNull() ?: lastKnownMe
            lastKnownMe = next
            return next
        }

        override suspend fun getTripLimits(): TripLimitsResponse = tripLimits
    }

    private class FakeHomeTripsDataSource(
        private val trips: List<TripDto> = emptyList()
    ) : HomeTripsDataSource {
        override suspend fun getMyTrips(): List<TripDto> = trips
    }

    private class FakeHomePaymentsDataSource(
        private val response: DummyPaymentResponse = DummyPaymentResponse(
            purchaseId = "purchase-1",
            provider = "DUMMY_PAY",
            status = "PENDING",
            plan = "PREMIUM",
            amount = "5.99",
            currency = "EUR",
            paymentUrl = "https://example.com/pay"
        )
    ) : HomePaymentsDataSource {
        val requests = mutableListOf<DummyPaymentPlan>()

        override suspend fun createDummyPayment(plan: DummyPaymentPlan): DummyPaymentResponse {
            requests += plan
            return response.copy(plan = plan.apiValue)
        }
    }
}
