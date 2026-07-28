package com.example.memotrip_kroniq.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.trips.TripsRepository
import com.example.memotrip_kroniq.ui.home.model.TripHistoryItem
import com.memotrip_kroniq.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val tripsRepository: TripsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadMe()
        loadTripLimits()
        loadTrips()
    }

    private fun loadMe() {
        viewModelScope.launch {
            try {
                val me = authRepository.getMe()

                _uiState.update {
                    it.copy(
                        isKroniq = me.isKroniq,
                        isThemesLocked = !me.isKroniq,
                        isLoading = false,
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isKroniq = false,
                        isThemesLocked = true,
                        isLoading = false
                    )
                }
            }
        }
    }


    private fun loadTrips(showLoader: Boolean = true) {
        viewModelScope.launch {
            if (showLoader) {
                _uiState.update { it.copy(isTripsLoading = true) }
            }

            try {
                val trips = tripsRepository.getMyTrips()
                if (BuildConfig.DEBUG) {
                    Log.d("HOME_TRIPS", "Loaded trips count=${trips.size}")
                }

                _uiState.update { state ->
                    state.copy(
                        trips = trips.map { trip ->
                            TripHistoryItem(
                                id = trip.id,
                                title = trip.title,
                                coverImageUrl = trip.coverImageUrl,
                                theme = trip.theme,
                                isSharedInKroniq = trip.isSharedInKroniQ
                            )
                        },
                        isTripsLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isTripsLoading = false)
                }
            }
        }
    }

    fun refreshTrips() {
        loadTrips(showLoader = false)
    }

    private fun loadTripLimits() {
        viewModelScope.launch {
            try {
                val limits = authRepository.getTripLimits()
                _uiState.update {
                    it.copy(
                        isAddTripEnabled = limits.allowed,
                        tripLimitPlan = limits.plan,
                        tripLimitUsed = limits.used,
                        tripLimitLimit = limits.limit
                    )
                }
            } catch (e: Exception) {
                // Fail-safe: bez potvrzených limitů nedovolíme otevřít Add Trip flow.
                _uiState.update { it.copy(isAddTripEnabled = false) }
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



}
