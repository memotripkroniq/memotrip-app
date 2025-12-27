package com.example.memotrip_kroniq.ui.addtrip

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.location.LocationSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.memotrip_kroniq.ui.addtrip.DateRange
import com.example.memotrip_kroniq.data.location.LocationSearchRepository
import com.example.memotrip_kroniq.data.network.HttpClientProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


class AddTripViewModel(
    private val authRepository: AuthRepository,
    private val locationSearchRepository: LocationSearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTripUiState())
    val uiState: StateFlow<AddTripUiState> = _uiState

    private var fromSearchJob: Job? = null
    private var toSearchJob: Job? = null


    init {
        _uiState.update { it.copy(isLoading = true) }
        loadMe()
    }

    private fun loadMe() {
        viewModelScope.launch {
            try {
                val me = authRepository.getMe()

                _uiState.value = _uiState.value.copy(
                    isThemesLocked = !me.isKroniq,   // 🔐 STEJNÉ PRAVIDLO JAKO HOME
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isThemesLocked = true,           // fail-safe
                    isLoading = false
                )
            }
        }
    }

    fun onTripNameChange(value: String) {
        _uiState.update {
            it.copy(tripName = value)
        }
    }

    fun onCoverPhotoSelected(uri: Uri?) {
        _uiState.update {
            it.copy(coverPhotoUri = uri)
        }
    }

    fun onDestinationSelected(destination: Destination) {
        _uiState.update {
            it.copy(destination = destination)
        }
    }

    fun onFromLocationChange(value: String) {
        _uiState.update { it.copy(fromLocation = value) }

        fromSearchJob?.cancel()

        if (value.length < 3) {
            _uiState.update {
                it.copy(fromSuggestions = emptyList())
            }
            return
        }

        fromSearchJob = viewModelScope.launch {
            delay(300)

            val results = locationSearchRepository.search(value)

            _uiState.update {
                it.copy(fromSuggestions = results)
            }
        }
    }


    fun onFromSuggestionSelected(suggestion: LocationSuggestion) {
        _uiState.update {
            it.copy(
                fromLocation = suggestion.displayName,
                fromSuggestions = emptyList()
            )
        }
    }

    fun onToLocationChange(value: String) {
        _uiState.update { it.copy(toLocation = value) }

        toSearchJob?.cancel()

        if (value.length < 3) {
            _uiState.update {
                it.copy(toSuggestions = emptyList())
            }
            return
        }

        toSearchJob = viewModelScope.launch {
            delay(300)

            val results = locationSearchRepository.search(value)

            _uiState.update {
                it.copy(toSuggestions = results)
            }
        }
    }

    fun onToSuggestionSelected(suggestion: LocationSuggestion) {
        _uiState.update {
            it.copy(
                toLocation = suggestion.displayName,
                toSuggestions = emptyList()
            )
        }
    }

    fun onThemeSelected(theme: ThemeType) {
        _uiState.update { state ->

            // ⛔ ještě nevíme, jestli je uživatel premium
            if (state.isLoading) {
                state
            }
            // 🔒 zamčeno
            else if (state.isThemesLocked) {
                state
            }
            // 🔓 odemčeno → toggle logika
            else {
                state.copy(
                    selectedTheme =
                        if (state.selectedTheme == theme) null
                        else theme
                )
            }
        }
    }

    fun onDateSelected(range: DateRange) {
        _uiState.update {
            it.copy(
                tripStartDate = range.start,
                tripEndDate = range.end,
                showDateError = false
            )
        }
    }

    fun onTransportSelectionChange(selected: Set<TransportType>) {
        _uiState.update {
            it.copy(transport = selected)
        }
    }


//    fun onNextClick() {
//        val hasTripName = uiState.tripName.isNotBlank()
//        val hasDestination = uiState.destination != null
//        val hasDate = uiState.tripDate != null
//        val hasFrom = uiState.fromLocation.isNotBlank()
//        val hasTo = uiState.toLocation.isNotBlank()
//        val hasTransport = uiState.transport != null
//
//        if (
//            hasTripName &&
//            hasDestination &&
//            hasDate &&
//            hasFrom &&
//            hasTo &&
//            hasTransport
//        ) {
//            // ✅ OK → pokračuj
//            navigateNext()
//        } else {
//            // ❌ zobraz validační chyby
//            _uiState.update {
//                it.copy(
//                    showTripNameError = !hasTripName,
//                    showDestinationError = !hasDestination,
//                    showDateError = !hasDate,
//                    showFromLocationError = !hasFrom,
//                    showToLocationError = !hasTo,
//                    showTransportError = !hasTransport
//                )
//            }
//        }
//    }



}
