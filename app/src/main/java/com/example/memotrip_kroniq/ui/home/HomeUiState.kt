package com.example.memotrip_kroniq.ui.home

import com.example.memotrip_kroniq.data.payments.DummyPaymentPlan
import com.example.memotrip_kroniq.ui.home.model.TripHistoryItem


sealed interface ThemesContentState {
    data object Grid : ThemesContentState
    data class ThemeTrips(val theme: String) : ThemesContentState
}

enum class DummyPayState {
    IDLE, CREATING, WAITING, VERIFYING, CONFIRMED, FAILED, UNCONFIRMED,
    VERIFICATION_ERROR, OPEN_ERROR, CREATION_UNKNOWN, START_ERROR, UNAVAILABLE
}


data class HomeUiState(
    val isThemesLocked: Boolean = true,   // 🔒 lock/unlock Themes
    val isLoading: Boolean = true,        // ⏳ loading Home
    val userEmail: String? = null,
    // 🆕 TRIPS
    val trips: List<TripHistoryItem> = emptyList(),
    val isTripsLoading: Boolean = false,
    val isRefreshingHome: Boolean = false,
    val isCheckingAddTripAccess: Boolean = false,
    val isPremium: Boolean = false,
    val isKroniq: Boolean = false,
    val isAddTripEnabled: Boolean = true,
    val tripLimitPlan: String? = null,
    val tripLimitUsed: Int? = null,
    val tripLimitLimit: Int? = null,
    val themesContentState: ThemesContentState = ThemesContentState.Grid,
    val dummyPayLoadingPlan: DummyPaymentPlan? = null,
    val dummyPayUrlToOpen: String? = null,
    val isAwaitingDummyPayReturn: Boolean = false,
    val dummyPayState: DummyPayState = DummyPayState.IDLE,
    val dummyPayPlan: DummyPaymentPlan? = null,
    val dummyPayPurchaseId: String? = null,
    val canReopenDummyPay: Boolean = false,
    val isDummyPayNoticeDismissed: Boolean = false,
    val isDummyPayVerificationNoticePending: Boolean = false,
    val isAccountVerified: Boolean = false

) {
    val currentPlan: HomePlanTier
        get() = resolveCurrentPlan(isPremium = isPremium, isKroniq = isKroniq)

    val isDummyPayNoticeCollapsed: Boolean
        get() = isDummyPayNoticeDismissed && dummyPayState in setOf(
            DummyPayState.WAITING, DummyPayState.VERIFYING, DummyPayState.UNCONFIRMED
        )

    val isPurchaseBlocked: Boolean
        get() = !isAccountVerified || dummyPayState !in setOf(
            DummyPayState.IDLE, DummyPayState.CONFIRMED, DummyPayState.FAILED, DummyPayState.START_ERROR,
            DummyPayState.UNAVAILABLE
        )
}
