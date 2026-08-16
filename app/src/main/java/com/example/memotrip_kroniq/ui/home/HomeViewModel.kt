package com.example.memotrip_kroniq.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.remote.dto.TripLimitsResponse
import com.example.memotrip_kroniq.data.trips.TripsRepository
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
    private val authRepository: AuthRepository,
    private val tripsRepository: TripsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState
    private var refreshHomeJob: Job? = null
    private var addTripAccessJob: Job? = null

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
            if (BuildConfig.DEBUG) {
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

    private fun resolveIsAddTripEnabled(limits: TripLimitsResponse?): Boolean? {
        if (limits == null) return null
        if (limits.limit == null) return true
        return limits.allowed
    }



}
