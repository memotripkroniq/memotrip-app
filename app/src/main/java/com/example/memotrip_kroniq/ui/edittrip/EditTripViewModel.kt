package com.example.memotrip_kroniq.ui.edittrip

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.location.LocationSuggestion
import com.example.memotrip_kroniq.data.remote.dto.TripDetailDto
import com.example.memotrip_kroniq.data.remote.dto.TripDetailUpdateDto
import com.example.memotrip_kroniq.data.tripmap.TripMapGenerator
import com.example.memotrip_kroniq.data.trips.TripsRepository
import com.example.memotrip_kroniq.ui.addtrip.AddTripUiState
import com.example.memotrip_kroniq.ui.addtrip.DateRange
import com.example.memotrip_kroniq.ui.core.model.Destination
import com.example.memotrip_kroniq.ui.core.model.ThemeType
import com.example.memotrip_kroniq.ui.core.model.TransportType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditTripViewModel(
    private val tripsRepository: TripsRepository,
    private val authRepository: AuthRepository,
    private val tripMapGenerator: TripMapGenerator,
    private val tripId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditTripUiState())
    val uiState: StateFlow<EditTripUiState> = _uiState.asStateFlow()

    private var originalTrip: TripDetailDto? = null
    private var saveAfterMapGeneration = false
    private var pendingOnSaveDone: (() -> Unit)? = null

    init {
        loadInitialData()
    }

    fun refreshTrip() {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInitialLoading = true, errorMessage = null) }

            try {
                val me = authRepository.getMe()
                val trip = tripsRepository.getTripDetail(tripId)
                originalTrip = trip

                _uiState.update {
                    it.copy(
                        formState = AddTripUiState(
                            coverImageUrl = trip.coverImageUrl,
                            tripName = trip.name.orEmpty(),
                            generatedMapImageUrl = trip.mapImageUrl,
                            isGeneratingMap = false,
                            isMapDirty = false,
                            hasTriedGenerate = false,
                            destination = trip.destination.toDestination(),
                            selectedTheme = trip.theme.toThemeType(),
                            isThemesLocked = !me.isKroniq,
                            tripStartDate = trip.startDate.toLocalDateOrNull(),
                            tripEndDate = trip.endDate.toLocalDateOrNull(),
                            fromLocation = trip.from.orEmpty().toTextFieldValue(),
                            toLocation = trip.to.orEmpty().toTextFieldValue(),
                            stops = trip.waypoints.map { waypoint -> waypoint.toTextFieldValue() },
                            stopSuggestions = List(trip.waypoints.size) { emptyList() },
                            showStopErrors = List(trip.waypoints.size) { false },
                            transport = trip.transport.toTransportSet(),
                            isLoading = false,
                            errorMessage = null
                        ),
                        isInitialLoading = false,
                        isSaving = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e("EDIT_TRIP", "Initial load failed", e)
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isSaving = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun onTripNameChange(value: String) {
        updateForm {
            copy(
                tripName = value,
                showTripNameError = false
            )
        }
    }

    fun onDestinationSelected(destination: Destination) {
        updateForm {
            copy(
                destination = destination,
                showDestinationError = false
            )
        }
    }

    fun onThemeSelected(theme: ThemeType) {
        updateForm {
            if (isLoading || isThemesLocked) {
                this
            } else {
                copy(
                    selectedTheme = if (selectedTheme == theme) null else theme
                )
            }
        }
    }

    fun onDateSelected(range: DateRange) {
        updateForm {
            copy(
                tripStartDate = range.start,
                tripEndDate = range.end,
                showDateError = false
            )
        }
    }

    fun onFromSuggestionSelected(suggestion: LocationSuggestion) {
        updateForm {
            copy(
                fromLocation = suggestion.displayName.toTextFieldValue(),
                showFromLocationError = false,
                isMapDirty = true
            )
        }
    }

    fun onToSuggestionSelected(suggestion: LocationSuggestion) {
        updateForm {
            copy(
                toLocation = suggestion.displayName.toTextFieldValue(),
                showToLocationError = false,
                isMapDirty = true
            )
        }
    }

    fun onAddStop() {
        updateForm {
            if (stops.size >= 3) return@updateForm this

            copy(
                stops = stops + TextFieldValue(""),
                stopSuggestions = stopSuggestions + emptyList(),
                showStopErrors = showStopErrors + false,
                focusedStopIndex = stops.size,
                isFromFocused = false,
                isToFocused = false,
                isMapDirty = true
            )
        }
    }

    fun onRemoveStop(index: Int) {
        updateForm {
            val newStops = stops.filterIndexed { i, _ -> i != index }

            copy(
                stops = newStops,
                stopSuggestions = stopSuggestions.filterIndexed { i, _ -> i != index },
                showStopErrors = List(newStops.size) { false },
                focusedStopIndex = null,
                isMapDirty = true
            )
        }
    }

    fun onStopSuggestionSelected(index: Int, suggestion: LocationSuggestion) {
        updateForm {
            if (index !in stops.indices) return@updateForm this

            val updatedStops = stops.toMutableList().apply {
                this[index] = suggestion.displayName.toTextFieldValue()
            }

            val updatedErrors = showStopErrors.toMutableList().apply {
                while (size < updatedStops.size) add(false)
                this[index] = false
            }

            copy(
                stops = updatedStops,
                showStopErrors = updatedErrors,
                focusedStopIndex = null,
                isMapDirty = true
            )
        }
    }

    fun onTransportSelectionChange(selected: Set<TransportType>) {
        updateForm {
            copy(
                transport = selected,
                isMapDirty = true,
                showTransportError = false
            )
        }
    }

    fun onCoverPhotoSelected(uri: Uri?) {
        if (uri == null) {
            updateForm {
                copy(
                    coverPhotoUri = null,
                    coverImageUrl = null,
                    isLoading = false,
                    errorMessage = null
                )
            }
            return
        }

        updateForm {
            copy(
                coverPhotoUri = uri,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val uploadedUrl = tripsRepository.uploadCoverImage(uri)
                updateForm {
                    copy(
                        coverImageUrl = uploadedUrl,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e("EDIT_TRIP", "Cover upload failed", e)
                updateForm {
                    copy(
                        isLoading = false,
                        errorMessage = "Upload cover failed"
                    )
                }
            }
        }
    }

    fun generateTripMap() {
        val formState = _uiState.value.formState
        val areStopsValid = validateStops()
        val hasMapInputError = formState.fromLocation.text.isBlank() ||
            formState.toLocation.text.isBlank() ||
            formState.transport.isEmpty()

        if (hasMapInputError || !areStopsValid) {
            updateForm {
                copy(
                    hasTriedGenerate = true,
                    showFromLocationError = fromLocation.text.isBlank(),
                    showToLocationError = toLocation.text.isBlank(),
                    showTransportError = transport.isEmpty()
                )
            }
            return
        }

        if (formState.isGeneratingMap) return

        viewModelScope.launch {
            updateForm { copy(isGeneratingMap = true, errorMessage = null) }

            try {
                val current = _uiState.value.formState
                val imageUrl = tripMapGenerator.generate(
                    from = current.fromLocation.text,
                    to = current.toLocation.text,
                    transport = current.transport.first(),
                    stops = current.stops.map { it.text.trim() }.filter { it.isNotBlank() }
                )

                updateForm {
                    copy(
                        generatedMapImageUrl = imageUrl,
                        isGeneratingMap = false,
                        isMapDirty = false,
                        showGeneratedMapError = false
                    )
                }

                if (saveAfterMapGeneration) {
                    saveAfterMapGeneration = false
                    val onDone = pendingOnSaveDone
                    pendingOnSaveDone = null
                    saveTripInternal(onDone)
                }
            } catch (e: Exception) {
                Log.e("EDIT_TRIP", "Map generation failed", e)
                saveAfterMapGeneration = false
                pendingOnSaveDone = null
                updateForm {
                    copy(
                        isGeneratingMap = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun onSaveClick(onDone: () -> Unit) {
        if (_uiState.value.isSaving) return

        val formState = _uiState.value.formState
        val areStopsValid = validateStops()

        val hasTripNameError = formState.tripName.isBlank()
        val hasDestinationError = formState.destination == null
        val hasDateError = formState.tripStartDate == null || formState.tripEndDate == null
        val hasFromError = formState.fromLocation.text.isBlank()
        val hasToError = formState.toLocation.text.isBlank()
        val hasTransportError = formState.transport.isEmpty()
        val isMapMissing = formState.generatedMapImageUrl == null

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
            updateForm {
                copy(
                    showTripNameError = hasTripNameError,
                    showDestinationError = hasDestinationError,
                    showDateError = hasDateError,
                    showFromLocationError = hasFromError,
                    showToLocationError = hasToError,
                    showTransportError = hasTransportError,
                    showGeneratedMapError = isMapMissing
                )
            }
            return
        }

        if (formState.isMapDirty) {
            saveAfterMapGeneration = true
            pendingOnSaveDone = onDone
            generateTripMap()
            return
        }

        saveTripInternal(onDone)
    }

    private fun saveTripInternal(onDone: (() -> Unit)? = null) {
        val trip = originalTrip ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            runCatching {
                val current = _uiState.value.formState
                val mapUrl = current.generatedMapImageUrl ?: trip.mapImageUrl
                val mapFullUrl =
                    if (current.generatedMapImageUrl != null && current.generatedMapImageUrl != trip.mapImageUrl) {
                        current.generatedMapImageUrl
                    } else {
                        trip.mapImageFullUrl ?: mapUrl
                    }

                val dto = TripDetailUpdateDto(
                    name = current.tripName.trim().takeIf { it.isNotBlank() },
                    destination = current.destination?.name,
                    transport = current.transport.firstOrNull()?.name?.lowercase(),
                    from = current.fromLocation.text.trim().takeIf { it.isNotBlank() },
                    to = current.toLocation.text.trim().takeIf { it.isNotBlank() },
                    waypoints = current.stops.map { it.text.trim() }.filter { it.isNotBlank() },
                    startDate = current.tripStartDate?.toString(),
                    endDate = current.tripEndDate?.toString(),
                    theme = current.selectedTheme?.name?.lowercase(),
                    coverImageUrl = current.coverImageUrl,
                    mapImageUrl = mapUrl,
                    mapImageFullUrl = mapFullUrl,
                    plannedBudget = trip.plannedBudget,
                    spentBudget = trip.spentBudget,
                    checklistItems = trip.tripChecklistItems
                        .sortedBy { it.order ?: Int.MAX_VALUE }
                        .mapIndexed { idx, item ->
                            TripDetailUpdateDto.ChecklistItemDto(
                                text = item.text.orEmpty(),
                                checked = item.checked ?: false,
                                order = idx
                            )
                        },
                    notes = trip.tripNotes
                        .sortedBy { it.order ?: Int.MAX_VALUE }
                        .mapIndexed { idx, note ->
                            TripDetailUpdateDto.NoteDto(
                                text = note.text.orEmpty(),
                                order = idx
                            )
                        },
                    tipsAndTrips = trip.tripTipsAndTrips
                        .sortedBy { it.order ?: Int.MAX_VALUE }
                        .mapIndexed { idx, tip ->
                            TripDetailUpdateDto.TipAndTripDto(
                                title = tip.title.orEmpty(),
                                imageUrl = tip.imageUrl,
                                order = idx
                            )
                        }
                )

                tripsRepository.updateTripDetail(tripId, dto)
            }.onSuccess { updatedTrip ->
                originalTrip = updatedTrip
                _uiState.update { it.copy(isSaving = false, errorMessage = null) }
                onDone?.invoke()
            }.onFailure { e ->
                Log.e("EDIT_TRIP", "Trip update failed", e)
                pendingOnSaveDone = null
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    private fun validateStops(): Boolean {
        val formState = _uiState.value.formState
        if (formState.stops.isEmpty()) {
            updateForm { copy(showStopErrors = emptyList()) }
            return true
        }

        val errors = formState.stops.map { it.text.isBlank() }
        updateForm { copy(showStopErrors = errors) }
        return errors.none { it }
    }

    private inline fun updateForm(transform: AddTripUiState.() -> AddTripUiState) {
        _uiState.update { state ->
            state.copy(
                formState = state.formState.transform()
            )
        }
    }

    private fun String?.toLocalDateOrNull(): LocalDate? =
        runCatching { this?.take(10)?.let(LocalDate::parse) }.getOrNull()

    private fun String?.toDestination(): Destination? =
        this?.let { raw -> Destination.entries.firstOrNull { it.name == raw.uppercase() } }

    private fun String?.toThemeType(): ThemeType? =
        this?.let { raw -> ThemeType.entries.firstOrNull { it.name == raw.uppercase() } }

    private fun String?.toTransportSet(): Set<TransportType> =
        this?.let { raw ->
            TransportType.entries.firstOrNull { it.name == raw.uppercase() }?.let(::setOf)
        } ?: emptySet()

    private fun String.toTextFieldValue(): TextFieldValue =
        TextFieldValue(text = this, selection = TextRange(length))
}
