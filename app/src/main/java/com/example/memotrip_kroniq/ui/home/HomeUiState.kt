package com.example.memotrip_kroniq.ui.home

import com.example.memotrip_kroniq.data.payments.DummyPaymentPlan
import com.example.memotrip_kroniq.ui.home.model.TripHistoryItem


sealed interface ThemesContentState {
    data object Grid : ThemesContentState
    data class ThemeTrips(val theme: String) : ThemesContentState
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
    val dummyPayErrorMessage: String? = null,
    val isAwaitingDummyPayReturn: Boolean = false

) {
    val currentPlan: HomePlanTier
        get() = resolveCurrentPlan(isPremium = isPremium, isKroniq = isKroniq)
}
