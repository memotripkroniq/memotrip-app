package com.example.memotrip_kroniq.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.data.payments.DummyPaymentPlan
import com.example.memotrip_kroniq.data.remote.dto.TripLimitsResponse
import com.example.memotrip_kroniq.ui.home.model.TripHistoryItem
import com.memotrip_kroniq.BuildConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: HomeAuthDataSource,
    private val tripsRepository: HomeTripsDataSource,
    private val paymentsRepository: HomePaymentsDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState
    private var refreshHomeJob: Job? = null
    private var addTripAccessJob: Job? = null
    private var paymentReturnRefreshJob: Job? = null

    private data class HomeRefreshPayload(
        val trips: List<TripHistoryItem>?,
        val limits: TripLimitsResponse?
    )

    init {
        loadMe()
        refreshHomeData(showTripLoader = true)
    }

    private fun loadMe() {
        viewModelScope.launch {
            try {
                applyUserState(authRepository.getMe(), isLoading = false)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPremium = false,
                        isKroniq = false,
                        isThemesLocked = true,
                        isLoading = false
                    )
                }
            }
        }
    }


    fun refreshHomeData(showTripLoader: Boolean = false) {
        if (refreshHomeJob?.isActive == true) return

        refreshHomeJob = viewModelScope.launch {
            if (showTripLoader) {
                _uiState.update {
                    it.copy(
                        isTripsLoading = true,
                        isRefreshingHome = false
                    )
                }
            } else {
                _uiState.update { it.copy(isRefreshingHome = true) }
            }

            try {
                val payload = fetchHomeRefreshPayload()
                applyHomeRefreshPayload(payload)

                if (payload.limits == null && showTripLoader) {
                    val fallbackEnabled = _uiState.value.isKroniq
                    _uiState.update { it.copy(isAddTripEnabled = fallbackEnabled) }
                }
            } finally {
                _uiState.update { it.copy(isTripsLoading = false, isRefreshingHome = false) }
            }
        }
    }

    fun verifyAddTripAccess(
        onAllowed: () -> Unit,
        onBlocked: () -> Unit = {}
    ) {
        if (addTripAccessJob?.isActive == true) return

        addTripAccessJob = viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAddTripAccess = true) }

            try {
                val payload = fetchHomeRefreshPayload()
                applyHomeRefreshPayload(payload)

                val limits = payload.limits
                val isAllowed = resolveIsAddTripEnabled(limits)

                if (isAllowed == true) {
                    onAllowed()
                } else if (isAllowed == false) {
                    onBlocked()
                }
            } catch (e: Exception) {
                // Keep the current state on transient verification failures.
            } finally {
                _uiState.update { it.copy(isCheckingAddTripAccess = false) }
            }
        }
    }

    fun onThemeClick(theme: String) {
        if (BuildConfig.DEBUG) {
            Log.d("HOME_THEME", "Theme selected")
        }

        _uiState.update {
            it.copy(
                themesContentState = ThemesContentState.ThemeTrips(theme)
            )
        }
    }

    fun onThemesBackClick() {
        if (BuildConfig.DEBUG) {
            Log.d("HOME_THEME", "Returned to theme grid")
        }

        _uiState.update {
            it.copy(
                themesContentState = ThemesContentState.Grid
            )
        }
    }

    fun getTripsForSelectedTheme(): List<TripHistoryItem> {
        val contentState = _uiState.value.themesContentState

        return if (contentState is ThemesContentState.ThemeTrips) {
            _uiState.value.trips.filter { trip ->
                trip.theme.equals(contentState.theme, ignoreCase = true)
            }
        } else {
            emptyList()
        }
    }

    fun startPremiumDummyPay() {
        startDummyPay(DummyPaymentPlan.PREMIUM)
    }

    fun startKroniqDummyPay() {
        startDummyPay(DummyPaymentPlan.KRONIQ)
    }

    fun onDummyPayBrowserOpened() {
        _uiState.update { it.copy(isAwaitingDummyPayReturn = true) }
    }

    fun refreshUserFromPaymentReturnIfNeeded() {
        if (!_uiState.value.isAwaitingDummyPayReturn) return
        if (paymentReturnRefreshJob?.isActive == true) return

        paymentReturnRefreshJob = viewModelScope.launch {
            try {
                applyUserState(authRepository.getMe())
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        dummyPayErrorMessage = e.message ?: "Unable to refresh payment status."
                    )
                }
            } finally {
                _uiState.update { it.copy(isAwaitingDummyPayReturn = false) }
            }
        }
    }

    fun consumeDummyPayUrl() {
        _uiState.update { it.copy(dummyPayUrlToOpen = null) }
    }

    fun consumeDummyPayError() {
        _uiState.update { it.copy(dummyPayErrorMessage = null) }
    }

    private suspend fun fetchHomeRefreshPayload(): HomeRefreshPayload = coroutineScope {
        val limitsDeferred = async { runCatching { authRepository.getTripLimits() }.getOrNull() }
        val tripsDeferred = async { runCatching { tripsRepository.getMyTrips() }.getOrNull() }

        val trips = tripsDeferred.await()?.map { trip ->
            TripHistoryItem(
                id = trip.id,
                title = trip.title,
                coverImageUrl = trip.coverImageUrl,
                theme = trip.theme,
                isSharedInKroniq = trip.isSharedInKroniQ
            )
        }

        HomeRefreshPayload(
            trips = trips,
            limits = limitsDeferred.await()
        )
    }

    private fun applyHomeRefreshPayload(payload: HomeRefreshPayload) {
        payload.trips?.let { trips ->
            if (BuildConfig.DEBUG && trips.isNotEmpty()) {
                Log.d("HOME_TRIPS", "Loaded trips count=${trips.size}")
            }

            _uiState.update { state ->
                state.copy(trips = trips)
            }
        }

        payload.limits?.let { limits ->
            val isAddTripEnabled = resolveIsAddTripEnabled(limits) ?: false
            _uiState.update {
                it.copy(
                    // `limit == null` is the backend contract for unlimited trips (KroniQ).
                    isAddTripEnabled = isAddTripEnabled,
                    tripLimitPlan = limits.plan,
                    tripLimitUsed = limits.used,
                    tripLimitLimit = limits.limit
                )
            }
        }
    }

    private fun applyUserState(
        me: com.example.memotrip_kroniq.data.model.UserMe,
        isLoading: Boolean = _uiState.value.isLoading
    ) {
        _uiState.update {
            it.copy(
                userEmail = me.email,
                isPremium = me.isPremium,
                isKroniq = me.isKroniq,
                isThemesLocked = !me.isKroniq,
                isLoading = isLoading
            )
        }
    }

    private fun resolveIsAddTripEnabled(limits: TripLimitsResponse?): Boolean? {
        if (limits == null) return null
        if (limits.limit == null) return true
        return limits.allowed
    }

    private fun startDummyPay(plan: DummyPaymentPlan) {
        if (BuildConfig.FLAVOR == "production") {
            _uiState.update {
                it.copy(dummyPayErrorMessage = "DummyPay is not available in production.")
            }
            return
        }

        if (isPlanAlreadyActive(plan)) return
        if (_uiState.value.dummyPayLoadingPlan != null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    dummyPayLoadingPlan = plan,
                    dummyPayErrorMessage = null,
                    dummyPayUrlToOpen = null
                )
            }

            try {
                val response = paymentsRepository.createDummyPayment(plan)
                val paymentUrl = response.paymentUrl.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("Payment URL is missing.")

                _uiState.update {
                    it.copy(
                        dummyPayLoadingPlan = null,
                        dummyPayUrlToOpen = paymentUrl
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        dummyPayLoadingPlan = null,
                        dummyPayErrorMessage = e.message ?: "Unable to start payment."
                    )
                }
            }
        }
    }

    private fun isPlanAlreadyActive(plan: DummyPaymentPlan): Boolean {
        return when (plan) {
            DummyPaymentPlan.PREMIUM -> _uiState.value.currentPlan == HomePlanTier.PREMIUM
            DummyPaymentPlan.KRONIQ -> _uiState.value.currentPlan == HomePlanTier.KRONIQ
        }
    }

}
