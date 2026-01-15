package com.example.memotrip_kroniq.ui.addtrip.utils

import androidx.compose.foundation.lazy.LazyListState
import com.example.memotrip_kroniq.ui.addtrip.AddTripUiState

/**
 * Jediný zdroj pravdy pro auto-scroll na první chybu
 */
suspend fun scrollToFirstAddTripError(
    listState: LazyListState,
    uiState: AddTripUiState
) {
    when {
        uiState.showTripNameError -> {
            listState.animateScrollToItem(AddTripScrollIndex.TRIP_NAME)
        }

        uiState.showDestinationError -> {
            listState.animateScrollToItem(AddTripScrollIndex.DESTINATION)
        }

        uiState.showDateError -> {
            listState.animateScrollToItem(AddTripScrollIndex.DATE)
        }

        uiState.showFromLocationError -> {
            listState.animateScrollToItem(AddTripScrollIndex.FROM)
        }

        uiState.showStopErrors.any { it } -> {
            val stopIndex = uiState.showStopErrors.indexOfFirst { it }
            if (stopIndex != -1) {
                listState.animateScrollToItem(
                    AddTripScrollIndex.firstStop(stopIndex)
                )
            }
        }

        uiState.showToLocationError -> {
            listState.animateScrollToItem(
                AddTripScrollIndex.to(uiState.stops.size)
            )
        }

        uiState.showTransportError -> {
            listState.animateScrollToItem(
                AddTripScrollIndex.transport(uiState.stops.size)
            )
        }
    }
}
