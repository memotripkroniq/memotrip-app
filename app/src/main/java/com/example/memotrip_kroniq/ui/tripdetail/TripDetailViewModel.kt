package com.example.memotrip_kroniq.ui.tripdetail

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.data.trips.TripsRepository
import com.example.memotrip_kroniq.ui.addtrip.ThemeType
import com.example.memotrip_kroniq.ui.addtrip.TransportType
import com.example.memotrip_kroniq.ui.tripdetail.components.ThemeUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class TripDetailViewModel(
    private val tripsRepository: TripsRepository,
    private val tripId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripDetailUiState())
    val uiState: StateFlow<TripDetailUiState> = _uiState.asStateFlow()

    init {
        loadTrip()
    }

    private fun loadTrip() {
        viewModelScope.launch {
            try {
                Log.d("TripDetailVM", "Loading tripId=$tripId")
                val trip = tripsRepository.getTripDetail(tripId)
                Log.d("TripDetailVM", "Loaded trip=$trip")

                val transportEnum = trip.transport?.let { raw ->
                    runCatching { TransportType.valueOf(raw.uppercase()) }.getOrNull()
                }

                val themeEnum = trip.theme?.let { raw ->
                    runCatching { ThemeType.valueOf(raw.uppercase()) }.getOrNull()
                }

                _uiState.value = _uiState.value.copy(
                    coverImageUrl = trip.coverImageUrl,
                    mapImageUrl = trip.mapImageUrl,
                    tripDateText = formatTripDate(trip.startDate, trip.endDate),
                    fromText = trip.from,
                    toText = trip.to,
                    transport = transportEnum?.let { setOf(it) } ?: emptySet(),
                    themes = listOf(
                        ThemeUi(ThemeType.SUMMER, R.drawable.homescreen_theme_summer),
                        ThemeUi(ThemeType.WINTER, R.drawable.homescreen_theme_winter),
                        ThemeUi(ThemeType.CAMPING, R.drawable.homescreen_theme_camping),
                        ThemeUi(ThemeType.CITIES, R.drawable.homescreen_theme_cities),
                        ThemeUi(ThemeType.NATURE, R.drawable.homescreen_theme_nature),
                        ThemeUi(ThemeType.EXOTIC, R.drawable.homescreen_theme_exotic)
                    ),
                    selectedTheme = themeEnum,
                    isThemesLocked = false,     // debug
                    hasKroniqPackage = true
                    // members/themes/notes/checklist zatím necháváme default (UI-only)
                )

            } catch (e: Exception) {
                Log.e("TripDetailVM", "loadTrip failed", e)
            }
        }
    }

    private fun formatTripDate(start: String, end: String): String {
        // ISO: "2026-02-04T00:00:00.000Z" → vezmeme jen YYYY-MM-DD
        val s = start.take(10)
        val e = end.take(10)
        return "$s - $e"
    }



    fun onTabSelected(tab: TripDetailTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onAddMemberClick() {
        // TODO
    }

    fun onDeleteTripClick() {
        // TODO
    }
}
