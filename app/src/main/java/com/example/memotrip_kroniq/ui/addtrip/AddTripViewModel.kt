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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.memotrip_kroniq.data.remote.dto.CreateTripRequest
import com.example.memotrip_kroniq.data.trips.TripsRepository


class AddTripViewModel(
    private val tripsRepository: TripsRepository,
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

    private var createAfterMapGeneration: Boolean = false



    init {

        _uiState.update { it.copy(isLoading = true) }
        loadMe()
    }


    // ─────────────────────────
    // 🔐 USER / PREMIUM
    // ─────────────────────────
    private fun loadMe() {
        viewModelScope.launch {
            try {
                val me = authRepository.getMe()

                _uiState.value = _uiState.value.copy(
                    isThemesLocked = !me.isKroniq,   // 🔐 STEJNÉ PRAVIDLO JAKO HOME
                    isLoading = false
                )

            //} catch (e: Exception) {
            //    _uiState.value = _uiState.value.copy(
            //        isThemesLocked = true,           // fail-safe
            //        isLoading = false
            //    )
            //}

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isThemesLocked = true,
                    isLoading = false
                )
            }
        }
        }
    }

    // ─────────────────────────
    // 📝 BASIC INPUTS
    // ─────────────────────────
    fun onTripNameChange(value: String) {
        _uiState.update {
            it.copy(
                tripName = value,
                showTripNameError = false
            )
        }
    }

    fun onCoverPhotoSelected(uri: Uri?) {
        Log.d("COVER_UPLOAD", "onCoverPhotoSelected called uri=$uri")

        // ✅ Delete photo
        if (uri == null) {
            _uiState.update {
                it.copy(
                    coverPhotoUri = null,
                    coverImageUrl = null,
                    errorMessage = null,
                    isLoading = false
                )
            }
            return
        }

        // ✅ Upload new photo
        _uiState.update {
            it.copy(
                coverPhotoUri = uri,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            Log.d("COVER_UPLOAD", "UPLOAD_START time=${System.currentTimeMillis()}")
            try {
                val uploadedUrl = tripsRepository.uploadCoverImage(uri)

                _uiState.update {
                    it.copy(
                        coverImageUrl = uploadedUrl,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e("COVER_UPLOAD", "UPLOAD_FAIL time=${System.currentTimeMillis()}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        coverImageUrl = null,
                        errorMessage = "Upload cover failed"
                    )
                }
            }
        }
    }

    // 🟢 NOVÉ – tichá validace pouze pro mapu
    private fun isMapInputValid(state: AddTripUiState): Boolean {
        return state.fromLocation.text.isNotBlank()
                && state.toLocation.text.isNotBlank()
                && state.transport.isNotEmpty()
    }

    // 🆕 WAYPOINT VALIDATION – jednotný pattern
    private fun validateStops(): Boolean {
        val state = _uiState.value

        if (state.stops.isEmpty()) {
            if (state.showStopErrors.isNotEmpty()) {
                _uiState.update { it.copy(showStopErrors = emptyList()) }
            }
            return true
        }

        val errors = state.stops.map { it.text.isBlank() }
        val hasError = errors.any { it }

        _uiState.update {
            it.copy(showStopErrors = errors)
        }

        return !hasError
    }

    fun generateTripMap() {
        val state = _uiState.value

        // 1️⃣ validace
        val isValid = isMapInputValid(state)
        val areStopsValid = validateStops()

        if (!isValid || !areStopsValid) {
            _uiState.update {
                it.copy(
                    hasTriedGenerate = true,
                    showFromLocationError = it.fromLocation.text.isBlank(),
                    showToLocationError = it.toLocation.text.isBlank(),
                    showTransportError = it.transport.isEmpty()
                )
            }
            return
        }

        if (state.isGeneratingMap) {
            Log.d(TAG, "⛔ already generating, ignoring click")
            return
        }

        // 2️⃣ 🔑 TADY JE TEN DŮLEŽITÝ KROK – waypointy
        val stops = state.stops
            .map { it.text.trim() }
            .filter { it.isNotBlank() }

        Log.d(TAG, "🧭 GENERATE MAP stops=$stops")

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingMap = true, errorMessage = null) }

            try {
                val imageUrl = tripMapGenerator.generate(
                    from = state.fromLocation.text,
                    to = state.toLocation.text,
                    transport = state.transport.first(),
                    stops = stops // ✅ WAYPOINTY POSLANÉ BACKENDU
                )

                Log.d(TAG, "✅ generated url=$imageUrl")

                _uiState.update {
                    it.copy(
                        generatedMapImageUrl = imageUrl,
                        isGeneratingMap = false,
                        isMapDirty = false,
                        showGeneratedMapError = false
                    )
                }
                if (createAfterMapGeneration) {
                    createAfterMapGeneration = false
                    saveTrip()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ generate failed", e)
                createAfterMapGeneration = false
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
            it.copy(
                destination = destination,
                showDestinationError = false
            )
        }
    }

    fun onFromLocationChange(value: TextFieldValue) {
        _uiState.update { it.copy(
            fromLocation = value,
            isMapDirty = true,
            showFromLocationError = false
        ) }

        fromSearchJob?.cancel()

        val query = value.text

        if (query.length < 4) {
            _uiState.update {
                it.copy(fromSuggestions = emptyList())
            }
            return
        }

        fromSearchJob = viewModelScope.launch {
            delay(300)

            val results = locationSearchRepository.search(query)

            _uiState.update {
                it.copy(fromSuggestions = results)
            }
        }
    }

    // 🆕 WAYPOINT – FOCUS CHANGE
    fun onStopFocusChanged(index: Int, focused: Boolean) {
        _uiState.update { state ->
            if (index !in state.stops.indices) return@update state

            val clearedSuggestions =
                if (!focused && index in state.stopSuggestions.indices) {
                    state.stopSuggestions.toMutableList().apply {
                        this[index] = emptyList()
                    }
                } else {
                    state.stopSuggestions
                }

            state.copy(
                focusedStopIndex = if (focused) index else null,
                stopSuggestions = clearedSuggestions,
                isFromFocused = false,
                isToFocused = false
            )
        }
    }


    fun onFromSuggestionSelected(suggestion: LocationSuggestion) {
        _uiState.update {
            it.copy(
                fromLocation = TextFieldValue(
                    text = suggestion.displayName,
                    selection = TextRange(suggestion.displayName.length)
                ),
                fromSuggestions = emptyList(),
                isFromFocused = false,
                showFromLocationError = false, // ✅ tohle chybělo
                isMapDirty = true              // 🔸 doporučené (aby se ukázalo, že mapa je “dirty”)
            )
        }
    }


    fun onToLocationChange(value: TextFieldValue) {
        _uiState.update { it.copy(
            toLocation = value,
            isMapDirty = true,
            showToLocationError = false
        ) }

        toSearchJob?.cancel()

        val query = value.text

        if (query.length < 4) {
            _uiState.update {
                it.copy(toSuggestions = emptyList())
            }
            return
        }

        toSearchJob = viewModelScope.launch {
            delay(300)

            val results = locationSearchRepository.search(query)
            _uiState.update {
                it.copy(toSuggestions = results)
            }
        }
    }

    fun onToSuggestionSelected(suggestion: LocationSuggestion) {
        _uiState.update {
            it.copy(
                toLocation = TextFieldValue(
                    text = suggestion.displayName,
                    selection = TextRange(suggestion.displayName.length)
                ),
                toSuggestions = emptyList(),
                isToFocused = false,
                showToLocationError = false,
                isMapDirty = true
            )
        }
    }


    // 🆕 WAYPOINTS – ADD
    fun onAddStop() {
        _uiState.update { state ->
            if (state.stops.size >= 3) return@update state

            state.copy(
                stops = state.stops + TextFieldValue(""),
                stopSuggestions = state.stopSuggestions + emptyList(),
                showStopErrors = state.showStopErrors + false,
                focusedStopIndex = state.stops.size,
                isFromFocused = false,
                isToFocused = false,
                isMapDirty = true
            )
        }
    }



    // 🆕 WAYPOINTS – REMOVE
    fun onRemoveStop(index: Int) {
        _uiState.update { state ->
            val newStops = state.stops.filterIndexed { i, _ -> i != index }

            state.copy(
                stops = newStops,
                stopSuggestions = state.stopSuggestions.filterIndexed { i, _ -> i != index },
                showStopErrors = List(newStops.size) { false },
                focusedStopIndex = null,   // ✅ ať nezůstane focus na neexistujícím indexu
                isMapDirty = true
            )
        }
    }


    // 🆕 WAYPOINTS – TEXT CHANGE + SEARCH
    fun onStopLocationChange(index: Int, value: TextFieldValue) {
        Log.d("WP_SEARCH", "index=$index text='${value.text}'")

        _uiState.update { state ->
            val stops = state.stops.toMutableList()
            stops[index] = value

            val errors = state.showStopErrors
                .toMutableList()
                .also { if (index in it.indices) it[index] = false }

            // ⚠️ GARANCE stejné velikosti
            val suggestions = state.stopSuggestions
                .toMutableList()
                .let {
                    if (it.size < stops.size) {
                        it + List(stops.size - it.size) { emptyList<LocationSuggestion>() }
                    } else it
                }

            state.copy(
                stops = stops,
                showStopErrors = errors,
                stopSuggestions = suggestions,
                isMapDirty = true
            )
        }

        val query = value.text
        if (query.length < 4) {
            _uiState.update { state ->
                state.copy(
                    stopSuggestions = state.stopSuggestions.mapIndexed { i, old ->
                        if (i == index) emptyList() else old
                    }
                )
            }
            return
        }

        Log.d("WP_SEARCH", "SEARCHING for '$query'")

        viewModelScope.launch {
            delay(300)
            val results = locationSearchRepository.search(query)

            Log.d("WP_SEARCH", "RESULTS size=${results.size}")

            _uiState.update { state ->
                state.copy(
                    stopSuggestions = state.stopSuggestions.mapIndexed { i, old ->
                        if (i == index) results else old
                    }
                )
            }
        }
    }


    // 🆕 WAYPOINTS – SELECT SUGGESTION
    fun onStopSuggestionSelected(index: Int, suggestion: LocationSuggestion) {
        _uiState.update { state ->
            if (index !in state.stops.indices) return@update state

            val updatedStops = state.stops.toMutableList().apply {
                this[index] = TextFieldValue(
                    text = suggestion.displayName,
                    selection = TextRange(suggestion.displayName.length)
                )
            }

            val updatedSuggestions =
                if (index in state.stopSuggestions.indices) {
                    state.stopSuggestions.toMutableList().apply { this[index] = emptyList() }
                } else state.stopSuggestions

            // ✅ RESET ERROR FLAGU PRO DANÝ WAYPOINT
            val updatedErrors = state.showStopErrors.toMutableList().apply {
                // pojistka na velikost
                while (size < updatedStops.size) add(false)
                this[index] = false
            }

            state.copy(
                stops = updatedStops,
                stopSuggestions = updatedSuggestions,
                showStopErrors = updatedErrors,
                focusedStopIndex = null,
                isMapDirty = true // 🔸 doporučené
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
            it.copy(
                transport = selected,
                isMapDirty = true,
                showTransportError = false
            )
        }
    }


    fun onCreateClick() {
        val state = _uiState.value
        val areStopsValid = validateStops()

        val hasTripNameError = state.tripName.isBlank()
        val hasDestinationError = state.destination == null
        val hasDateError = state.tripStartDate == null || state.tripEndDate == null
        val hasFromError = state.fromLocation.text.isBlank()
        val hasToError = state.toLocation.text.isBlank()
        val hasTransportError = state.transport.isEmpty()
        val isMapMissing = state.generatedMapImageUrl == null
        val isMapDirty = state.isMapDirty


        if (
            hasTripNameError ||
            hasDestinationError ||
            hasDateError ||
            hasFromError ||
            hasToError ||
            hasTransportError ||
            isMapMissing ||
            !areStopsValid
        ) {
            _uiState.update {
                it.copy(
                    showTripNameError = hasTripNameError,
                    showDestinationError = hasDestinationError,
                    showDateError = hasDateError,
                    showFromLocationError = hasFromError,
                    showToLocationError = hasToError,
                    showTransportError = hasTransportError,
                    showGeneratedMapError = isMapMissing,
                )
            }
            return
        }

        // ✅ Mapa existuje, ale je zastaralá → přegeneruj automaticky a pak ulož
        if (isMapDirty) {
            createAfterMapGeneration = true
            generateTripMap()
            return
        }

        saveTrip()
    }


    private fun saveTrip() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    flowState = AddTripFlowState.SAVING,
                    errorMessage = null
                )
            }

            try {
                val state = _uiState.value

                Log.d("CREATE_TRIP", "state.coverPhotoUri=${state.coverPhotoUri}")
                Log.d("CREATE_TRIP", "state.coverImageUrl=${state.coverImageUrl}")
                Log.d("CREATE_TRIP", "isLoading=${state.isLoading}")

                // ✅ cover už je uploadnutý v onCoverPhotoSelected()
                val coverUrl = state.coverImageUrl

                val request = CreateTripRequest(
                    name = state.tripName,
                    destination = state.destination!!.name,
                    dateFrom = state.tripStartDate!!.toString(),
                    dateTo = state.tripEndDate!!.toString(),
                    from = state.fromLocation.text,
                    to = state.toLocation.text,
                    transport = state.transport.first().name,
                    waypoints = state.stops.map { it.text },
                    theme = state.selectedTheme?.name,
                    coverImageUrl = coverUrl // ✅ TADY
                )

                Log.d("CREATE_TRIP", "request.coverImageUrl=${request.coverImageUrl}")

                tripsRepository.createTrip(request)

                _uiState.update { it.copy(flowState = AddTripFlowState.SUCCESS) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        flowState = AddTripFlowState.ERROR,
                        errorMessage = e.message ?: "Trip saving failed"
                    )
                }
            }
        }
    }

}
