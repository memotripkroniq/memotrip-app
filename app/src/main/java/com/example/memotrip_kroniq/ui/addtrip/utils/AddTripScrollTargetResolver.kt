package com.example.memotrip_kroniq.ui.addtrip.utils

import com.example.memotrip_kroniq.ui.addtrip.AddTripUiState

fun resolveFirstAddTripScrollTarget(
    uiState: AddTripUiState
): AddTripScrollTarget? {

    return when {
        uiState.showTripNameError -> AddTripScrollTarget.TRIP_NAME

        uiState.showDestinationError ->
            AddTripScrollTarget.DESTINATION

        uiState.showDateError ->
            AddTripScrollTarget.DATE

        uiState.showFromLocationError ->
            AddTripScrollTarget.ROUTE_BLOCK

        uiState.showStopErrors.any { it } ->
            AddTripScrollTarget.ROUTE_BLOCK

        uiState.showToLocationError ->
            AddTripScrollTarget.ROUTE_BLOCK

        uiState.showTransportError ->
            AddTripScrollTarget.TRANSPORT

        else -> null
    }
}
