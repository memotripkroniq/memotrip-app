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
import com.example.memotrip_kroniq.data.tripmap.TripMapGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.util.Log


class AddTripViewModel(
    private val authRepository: AuthRepository,
    private val locationSearchRepository: LocationSearchRepository,
    private val tripMapGenerator: TripMapGenerator
) : ViewModel() {

    companion object {
        private const val TAG = "AddTripVM"
    }

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

    fun generateTripMap() {
        val state = _uiState.value

        Log.d(TAG, "🔥 generateTripMap() click")
        Log.d(TAG, "from='${state.fromLocation}', to='${state.toLocation}', transport=${state.transport}")

        // 🟥 VALIDACE PRO GENERATE MAPY
        val hasFromError = state.fromLocation.isBlank()
        val hasToError = state.toLocation.isBlank()
        val hasTransportError = state.transport.isEmpty()

        if (hasFromError || hasToError || hasTransportError) {
            Log.d(TAG, "⛔ validation failed: from=$hasFromError to=$hasToError transport=$hasTransportError")
            _uiState.update {
                it.copy(
                    showFromLocationError = hasFromError,
                    showToLocationError = hasToError,
                    showTransportError = hasTransportError
                )
            }
            return
        }

        // ⛔ už se generuje
        if (state.isGeneratingMap) {
            Log.d(TAG, "⛔ already generating, ignoring click")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingMap = true) }

            try {
                val imageUrl = tripMapGenerator.generate(
                    from = state.fromLocation,
                    to = state.toLocation,
                    transport = state.transport.first() // zatím 1
                )

                Log.d(TAG, "✅ generated url=$imageUrl")
                _uiState.update {
                    it.copy(
                        generatedMapImageUrl = imageUrl,
                        isGeneratingMap = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ generate failed", e)
                _uiState.update {
                    it.copy(
                        isGeneratingMap = false,
                        errorMessage = e.message
                    )
                }
            }
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


    fun onCreateClick() {
        val state = _uiState.value

        // 🔴 validace (STEJNÁ JAKO TEĎ – OK)
        val hasTripNameError = state.tripName.isBlank()
        val hasDestinationError = state.destination == null
        val hasDateError = state.tripStartDate == null || state.tripEndDate == null
        val hasFromError = state.fromLocation.isBlank()
        val hasToError = state.toLocation.isBlank()
        val hasTransportError = state.transport.isEmpty()

        if (
            hasTripNameError ||
            hasDestinationError ||
            hasDateError ||
            hasFromError ||
            hasToError ||
            hasTransportError
        ) {
            _uiState.update {
                it.copy(
                    showTripNameError = hasTripNameError,
                    showDestinationError = hasDestinationError,
                    showDateError = hasDateError,
                    showFromLocationError = hasFromError,
                    showToLocationError = hasToError,
                    showTransportError = hasTransportError
                )
            }
            return
        }

        // ✅ TADY UŽ SE MAPA NEGENERUJE
        // tady bude save / pokračování flow
    }

}
