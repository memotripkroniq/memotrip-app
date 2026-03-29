package com.example.memotrip_kroniq.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.trips.TripsRepository
import com.example.memotrip_kroniq.ui.home.model.TripHistoryItem
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


    private fun loadTrips() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTripsLoading = true)

            try {
                val trips = tripsRepository.getMyTrips()
                android.util.Log.d("HOME_TRIPS", trips.toString())

                _uiState.value = _uiState.value.copy(
                    trips = trips.map {
                        TripHistoryItem(
                            id = it.id,
                            title = it.title,
                            coverImageUrl = it.coverImageUrl, // může být null
                            theme = it.theme
                        )
                    },
                    isTripsLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTripsLoading = false
                )
            }
        }
    }

    fun refreshTrips() {
        loadTrips()
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
                // MVP: když to selže, necháme tlačítko enabled
                _uiState.update { it.copy(isAddTripEnabled = true) }
            }
        }
    }

    fun onThemeClick(theme: String) {
        android.util.Log.d("HOME_THEME", "clicked theme = $theme")

        _uiState.update {
            it.copy(
                themesContentState = ThemesContentState.ThemeTrips(theme)
            )
        }
    }

    fun onThemesBackClick() {
        android.util.Log.d("HOME_THEME", "back to grid")

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
