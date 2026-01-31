package com.example.memotrip_kroniq.ui.home

import com.example.memotrip_kroniq.ui.home.model.TripHistoryItem

data class HomeUiState(
    val isThemesLocked: Boolean = true,   // 🔒 lock/unlock Themes
    val isLoading: Boolean = true,        // ⏳ loading Home
    val userEmail: String? = null,
    // 🆕 TRIPS
    val trips: List<TripHistoryItem> = emptyList(),
    val isTripsLoading: Boolean = false,
    val isKroniq: Boolean = false,
    val isAddTripEnabled: Boolean = true,
    val tripLimitPlan: String? = null,
    val tripLimitUsed: Int? = null,
    val tripLimitLimit: Int? = null,

    )