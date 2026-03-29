package com.example.memotrip_kroniq.ui.tripdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.data.trips.TripsRepository
import com.example.memotrip_kroniq.ui.core.model.ThemeType
import com.example.memotrip_kroniq.ui.core.model.TransportType
import com.example.memotrip_kroniq.ui.tripdetail.components.ThemeUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.memotrip_kroniq.ui.tripdetail.components.ChecklistItemUi
import kotlinx.coroutines.flow.update
import com.example.memotrip_kroniq.ui.tripdetail.components.NoteItemUi
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.memotrip_kroniq.ui.tripdetail.components.BudgetEditField
import android.net.Uri
import com.example.memotrip_kroniq.data.remote.dto.TripDetailUpdateDto
import com.example.memotrip_kroniq.data.remote.dto.TripDetailDto
import com.example.memotrip_kroniq.ui.tripdetail.components.TipsAndTripsItemUi


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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

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

                // ✅ checklist / notes / tips (seřadit podle order, ošetřit nulls)
                val checklistUi = trip.tripChecklistItems
                    .sortedBy { it.order ?: Int.MAX_VALUE }
                    .map {
                        ChecklistItemUi(
                            text = it.text.orEmpty(),
                            checked = it.checked ?: false
                        )
                    }

                val notesUi = trip.tripNotes
                    .sortedBy { it.order ?: Int.MAX_VALUE }
                    .map { NoteItemUi(text = it.text.orEmpty()) }

                val tipsUi = trip.tripTipsAndTrips
                    .sortedBy { it.order ?: Int.MAX_VALUE }
                    .map {
                        TipsAndTripsItemUi(
                            title = it.title.orEmpty(),
                            imageUri = it.imageUrl?.let(Uri::parse)
                        )
                    }

                _uiState.update { state ->
                    state.copy(
                        // core (v dto některé non-null, ale dávám safe i tak)
                        coverImageUrl = trip.coverImageUrl,
                        mapImageUrl = trip.mapImageUrl,
                        mapFullImageUrl = trip.mapImageFullUrl ?: trip.mapImageUrl,
                        tripDateText = formatTripDate(
                            trip.startDate.orEmpty(),
                            trip.endDate.orEmpty()
                        ),
                        fromText = trip.from.orEmpty(),
                        toText = trip.to.orEmpty(),
                        transport = transportEnum?.let { setOf(it) } ?: emptySet(),

                        // themes
                        themes = listOf(
                            ThemeUi(ThemeType.SUMMER, R.drawable.homescreen_theme_summer),
                            ThemeUi(ThemeType.WINTER, R.drawable.homescreen_theme_winter),
                            ThemeUi(ThemeType.CAMPING, R.drawable.homescreen_theme_camping),
                            ThemeUi(ThemeType.CITIES, R.drawable.homescreen_theme_cities),
                            ThemeUi(ThemeType.NATURE, R.drawable.homescreen_theme_nature),
                            ThemeUi(ThemeType.EXOTIC, R.drawable.homescreen_theme_exotic),
                        ),
                        selectedTheme = themeEnum,
                        isThemesLocked = false,

                        // budget
                        plannedBudget = trip.plannedBudget.orEmpty(),
                        spentBudget = trip.spentBudget.orEmpty(),

                        // lists
                        checklistItems = checklistUi,
                        editingChecklistIndex = null,
                        editingChecklistText = TextFieldValue(""),

                        notes = notesUi,
                        editingNoteIndex = null,
                        editingNoteText = TextFieldValue(""),

                        tipsAndTripsItems = tipsUi,
                        isTipsAndTripsAdding = false,
                        editingTipsIndex = null,
                        editingTipsText = TextFieldValue(""),
                        pickingTipsPhotoIndex = null,

                        // flags
                        hasKroniqPackage = true,
                        hasUnsavedChanges = false,
                        isLoading = false,
                        errorMessage = null
                    )
                }

                markClean()
            } catch (e: Exception) {
                Log.e("TripDetailVM", "loadTrip failed", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun formatTripDate(start: String?, end: String?): String {
        val s = start.orEmpty().take(10)
        val e = end.orEmpty().take(10)

        return when {
            s.isNotBlank() && e.isNotBlank() -> "$s - $e"
            s.isNotBlank() -> s
            e.isNotBlank() -> e
            else -> ""
        }
    }

    private fun markClean() {
        _uiState.update { it.copy(hasUnsavedChanges = false) }
    }

    private fun markDirty() {
        _uiState.update { it.copy(hasUnsavedChanges = true) }
    }

    fun onTabSelected(tab: TripDetailTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onFromTextChange(text: String) {
        _uiState.update { it.copy(fromText = text, hasUnsavedChanges = true) }
    }

    fun onToTextChange(text: String) {
        _uiState.update { it.copy(toText = text, hasUnsavedChanges = true) }
    }

    fun onThemeSelected(theme: ThemeType?) {
        _uiState.update { it.copy(selectedTheme = theme, hasUnsavedChanges = true) }
    }

    fun toggleTransport(transport: TransportType) {
        _uiState.update { state ->
            val current = state.transport.toMutableSet()

            val next = when {
                // už je vybraný -> odeber
                current.contains(transport) -> {
                    current.remove(transport)
                    current
                }

                // ještě není vybraný a máme místo (<2) -> přidej
                current.size < 2 -> {
                    current.add(transport)
                    current
                }

                // už jsou 2 a uživatel klikl na třetí -> ignoruj (nebo změň chování níže)
                else -> current
            }

            state.copy(
                transport = next,
                hasUnsavedChanges = state.hasUnsavedChanges || (next != state.transport)
            )
        }
    }

    fun addChecklistItem() {
        _uiState.update { state ->
            val items = state.checklistItems.toMutableList()

            val editingIndex = state.editingChecklistIndex
            if (editingIndex != null && editingIndex in items.indices) {
                val text = state.editingChecklistText.text.trim()

                if (text.isBlank()) {
                    items.removeAt(editingIndex)
                } else {
                    items[editingIndex] = items[editingIndex].copy(text = text)
                }
            }

            val newIndex = items.size
            items.add(ChecklistItemUi(text = "", checked = false))

            state.copy(
                checklistItems = items,
                editingChecklistIndex = newIndex,
                editingChecklistText = TextFieldValue("")   // ✅
            )
        }
    }


    fun startEditChecklistItem(index: Int) {
        _uiState.update { state ->
            val current = state.checklistItems.getOrNull(index)?.text ?: ""
            state.copy(
                editingChecklistIndex = index,
                editingChecklistText = TextFieldValue(
                    text = current,
                    selection = TextRange(current.length) // ✅ kurzor na konec
                )
            )
        }
    }

    fun updateEditingChecklistText(value: TextFieldValue) {
        _uiState.update { state -> state.copy(editingChecklistText = value) }
    }

    fun commitEditChecklistItem() {
        _uiState.update { state ->
            val index = state.editingChecklistIndex ?: return@update state
            val text = state.editingChecklistText.text.trim()

            val items = state.checklistItems.toMutableList()

            if (text.isBlank()) {
                if (index in items.indices) items.removeAt(index)
            } else {
                if (index in items.indices) items[index] = items[index].copy(text = text)
            }

            state.copy(
                checklistItems = items,
                editingChecklistIndex = null,
                editingChecklistText = TextFieldValue(""),
                hasUnsavedChanges = true
            )
        }
    }

    fun cancelEditChecklistItem() {
        _uiState.update { state ->
            state.copy(editingChecklistIndex = null, editingChecklistText = TextFieldValue(""))
        }
    }


    fun toggleChecklistItem(index: Int) {
        _uiState.update { state ->
            val items = state.checklistItems.toMutableList()
            if (index in items.indices) {
                val item = items[index]
                items[index] = item.copy(checked = !item.checked)
            }
            state.copy(checklistItems = items, hasUnsavedChanges = true)
        }
    }

    fun removeChecklistItem(index: Int) {
        _uiState.update { state ->
            val items = state.checklistItems.toMutableList()
            if (index in items.indices) items.removeAt(index)
            state.copy(checklistItems = items, hasUnsavedChanges = true)
        }
    }

    // -------- NOTES (Keep-like) --------

    fun addNoteItem() {
        _uiState.update { state ->
            val items = state.notes.toMutableList()

            val editingIndex = state.editingNoteIndex
            if (editingIndex != null && editingIndex in items.indices) {
                val text = state.editingNoteText.text.trim()

                if (text.isBlank()) {
                    items.removeAt(editingIndex)
                } else {
                    items[editingIndex] = items[editingIndex].copy(text = text)
                }
            }

            val newIndex = items.size
            items.add(NoteItemUi(text = ""))

            state.copy(
                notes = items,
                editingNoteIndex = newIndex,
                editingNoteText = TextFieldValue("") // ✅
            )
        }
    }

    fun startEditNoteItem(index: Int) {
        _uiState.update { state ->
            val current = state.notes.getOrNull(index)?.text ?: ""
            state.copy(
                editingNoteIndex = index,
                editingNoteText = TextFieldValue(
                    text = current,
                    selection = TextRange(current.length) // ✅ kurzor na konec
                )
            )
        }
    }

    fun updateEditingNoteText(value: TextFieldValue) {
        _uiState.update { state -> state.copy(editingNoteText = value) }
    }

    fun commitEditNoteItem() {
        _uiState.update { state ->
            val index = state.editingNoteIndex ?: return@update state
            val text = state.editingNoteText.text.trim()

            val items = state.notes.toMutableList()

            if (text.isBlank()) {
                if (index in items.indices) items.removeAt(index)
            } else {
                if (index in items.indices) items[index] = items[index].copy(text = text)
            }

            state.copy(
                notes = items,
                editingNoteIndex = null,
                editingNoteText = TextFieldValue(""),
                hasUnsavedChanges = true
            )
        }
    }

    fun cancelEditNoteItem() {
        _uiState.update { state ->
            state.copy(editingNoteIndex = null, editingNoteText = TextFieldValue(""))
        }
    }

    fun removeNoteItem(index: Int) {
        _uiState.update { state ->
            val items = state.notes.toMutableList()
            if (index in items.indices) items.removeAt(index)
            state.copy(notes = items, hasUnsavedChanges = true)
        }
    }

    fun toggleBudgetVisibility() {
        _uiState.update { state ->
            val newVisible = !state.isBudgetVisible
            val committed = commitBudgetEditInternal(state)

            if (!newVisible) {
                committed.copy(
                    isBudgetVisible = false,
                    editingBudgetField = null,
                    editingBudgetText = TextFieldValue("")
                )
            } else {
                committed.copy(isBudgetVisible = true)
            }
        }
    }



    fun startEditBudget(field: BudgetEditField) {
        _uiState.update { state ->
            // commit předchozí editace budgetu (pokud běžela)
            val committed = commitBudgetEditInternal(state)

            val currentText = when (field) {
                BudgetEditField.PLANNED -> committed.plannedBudget
                BudgetEditField.SPENT -> committed.spentBudget
            }

            committed.copy(
                editingBudgetField = field,
                editingBudgetText = TextFieldValue(
                    text = currentText,
                    selection = TextRange(currentText.length) // ✅ kurzor na konci
                )
            )
        }
    }

    fun updateEditingBudgetText(value: TextFieldValue) {
        _uiState.update { it.copy(editingBudgetText = value) }
    }

    fun commitEditBudget() {
        _uiState.update { state ->
            val committed = commitBudgetEditInternal(state)
            committed.copy(
                editingBudgetField = null,
                editingBudgetText = TextFieldValue(""),
                hasUnsavedChanges = true
            )
        }
    }

    fun cancelEditBudget() {
        _uiState.update {
            it.copy(editingBudgetField = null, editingBudgetText = TextFieldValue(""))
        }
    }

    // --- helper: commit podle editingBudgetField ---
    private fun commitBudgetEditInternal(state: TripDetailUiState): TripDetailUiState {
        val field = state.editingBudgetField ?: return state
        val text = state.editingBudgetText.text.trim()

        return when (field) {
            BudgetEditField.PLANNED -> state.copy(plannedBudget = text)
            BudgetEditField.SPENT -> state.copy(spentBudget = text)
        }
    }

    // -------- TIPS & TRIPS (Keep-like) --------

    fun addTipsAndTripsItem() {
        _uiState.update { state ->
            val items = state.tipsAndTripsItems.toMutableList()

            // 1) commit rozeditovaného itemu, pokud existuje
            val editingIndex = state.editingTipsIndex
            if (editingIndex != null && editingIndex in items.indices) {
                val text = state.editingTipsText.text.trim()
                if (text.isBlank()) {
                    items.removeAt(editingIndex)
                } else {
                    items[editingIndex] = items[editingIndex].copy(title = text)
                }
            }

            // 2) přidat nový item a přepnout editaci na něj
            val newIndex = items.size
            items.add(TipsAndTripsItemUi(title = "", imageUri = null))

            state.copy(
                tipsAndTripsItems = items,
                isTipsAndTripsAdding = true,
                editingTipsIndex = newIndex,
                editingTipsText = TextFieldValue(""),
                pickingTipsPhotoIndex = null
            )
        }
    }

    fun cancelAddTipsAndTrips() {
        _uiState.update { state ->
            state.copy(
                isTipsAndTripsAdding = false,
                editingTipsIndex = null,
                editingTipsText = TextFieldValue(""),
                pickingTipsPhotoIndex = null
            )
        }
    }

    fun startEditTipsAndTripsItem(index: Int) {
        _uiState.update { state ->
            val current = state.tipsAndTripsItems.getOrNull(index)?.title ?: ""
            state.copy(
                isTipsAndTripsAdding = true,
                editingTipsIndex = index,
                editingTipsText = TextFieldValue(
                    text = current,
                    selection = TextRange(current.length) // kurzor na konec
                )
            )
        }
    }

    fun updateEditingTipsText(value: TextFieldValue) {
        _uiState.update { it.copy(editingTipsText = value) }
    }

    fun commitEditTipsAndTrips() {
        _uiState.update { state ->
            val index = state.editingTipsIndex ?: return@update state
            val text = state.editingTipsText.text.trim()

            val items = state.tipsAndTripsItems.toMutableList()
            if (index !in items.indices) return@update state

            if (text.isBlank()) items.removeAt(index)
            else items[index] = items[index].copy(title = text)

            state.copy(
                tipsAndTripsItems = items,
                isTipsAndTripsAdding = false,
                editingTipsIndex = null,
                editingTipsText = TextFieldValue(""),
                pickingTipsPhotoIndex = null,
                hasUnsavedChanges = true
            )
        }
    }

    fun removeTipsAndTripsItem(index: Int) {
        _uiState.update { state ->
            val items = state.tipsAndTripsItems.toMutableList()
            if (index !in items.indices) return@update state
            items.removeAt(index)

            val editingIndex = state.editingTipsIndex
            val pickingIndex = state.pickingTipsPhotoIndex

            val newEditingIndex = when {
                editingIndex == null -> null
                index == editingIndex -> null                 // smazal jsem editovaný -> konec editace
                index < editingIndex -> editingIndex - 1      // smazal jsem před editovaným -> posun indexu
                else -> editingIndex                           // smazal jsem za editovaným -> beze změny
            }

            val newPickingIndex = when {
                pickingIndex == null -> null
                index == pickingIndex -> null
                index < pickingIndex -> pickingIndex - 1
                else -> pickingIndex
            }

            state.copy(
                tipsAndTripsItems = items,
                hasUnsavedChanges = true,
                editingTipsIndex = newEditingIndex,
                editingTipsText = if (newEditingIndex == null) TextFieldValue("") else state.editingTipsText,
                isTipsAndTripsAdding = newEditingIndex != null,
                pickingTipsPhotoIndex = newPickingIndex
            )
        }
    }


    // UI si zavolá před otevřením galerie (řekneme, pro který item vybíráme fotku)
    fun requestPickTipsPhoto(index: Int) {
        _uiState.update { it.copy(pickingTipsPhotoIndex = index) }
    }

    // callback z pickeru v UI
    fun onTipsPhotoPicked(uri: Uri?) {
        if (uri == null) return

        _uiState.update { state ->
            val index = state.pickingTipsPhotoIndex ?: return@update state
            val items = state.tipsAndTripsItems.toMutableList()
            if (index !in items.indices) return@update state.copy(pickingTipsPhotoIndex = null)

            items[index] = items[index].copy(imageUri = uri)

            state.copy(
                tipsAndTripsItems = items,
                pickingTipsPhotoIndex = null,
                hasUnsavedChanges = true
            )
        }
    }

    private fun commitAllEditingBeforeSave() {

        // Budget
        _uiState.update { state ->
            val beforePlanned = state.plannedBudget
            val beforeSpent = state.spentBudget
            val beforeField = state.editingBudgetField
            val beforeText = state.editingBudgetText

            val committed = commitBudgetEditInternal(state)

            val budgetChanged =
                (committed.plannedBudget != beforePlanned) ||
                        (committed.spentBudget != beforeSpent) ||
                        (beforeField != null) || (beforeText.text.isNotBlank())

            committed.copy(
                editingBudgetField = null,
                editingBudgetText = TextFieldValue(""),
                hasUnsavedChanges = committed.hasUnsavedChanges || budgetChanged
            )
        }

        // Checklist
        _uiState.update { state ->
            val index = state.editingChecklistIndex ?: return@update state
            val text = state.editingChecklistText.text.trim()

            val before = state.checklistItems
            val items = state.checklistItems.toMutableList()

            if (index !in items.indices) {
                return@update state.copy(
                    editingChecklistIndex = null,
                    editingChecklistText = TextFieldValue("")
                )
            }

            if (text.isBlank()) items.removeAt(index)
            else items[index] = items[index].copy(text = text)

            val after = items.toList()

            state.copy(
                checklistItems = after,
                editingChecklistIndex = null,
                editingChecklistText = TextFieldValue(""),
                hasUnsavedChanges = state.hasUnsavedChanges || (before != after)
            )
        }

        // Notes
        _uiState.update { state ->
            val index = state.editingNoteIndex ?: return@update state
            val text = state.editingNoteText.text.trim()

            val before = state.notes
            val items = state.notes.toMutableList()

            if (index !in items.indices) {
                return@update state.copy(
                    editingNoteIndex = null,
                    editingNoteText = TextFieldValue("")
                )
            }

            if (text.isBlank()) items.removeAt(index)
            else items[index] = items[index].copy(text = text)

            val after = items.toList()

            state.copy(
                notes = after,
                editingNoteIndex = null,
                editingNoteText = TextFieldValue(""),
                hasUnsavedChanges = state.hasUnsavedChanges || (before != after)
            )
        }

        // Tips & Trips
        _uiState.update { state ->
            val index = state.editingTipsIndex ?: return@update state
            val text = state.editingTipsText.text.trim()

            val before = state.tipsAndTripsItems
            val items = state.tipsAndTripsItems.toMutableList()

            if (index !in items.indices) {
                return@update state.copy(
                    isTipsAndTripsAdding = false,
                    editingTipsIndex = null,
                    editingTipsText = TextFieldValue(""),
                    pickingTipsPhotoIndex = null
                )
            }

            if (text.isBlank()) items.removeAt(index)
            else items[index] = items[index].copy(title = text)

            val after = items.toList()

            state.copy(
                tipsAndTripsItems = after,
                isTipsAndTripsAdding = false,
                editingTipsIndex = null,
                editingTipsText = TextFieldValue(""),
                pickingTipsPhotoIndex = null,
                hasUnsavedChanges = state.hasUnsavedChanges || (before != after)
            )
        }
    }

    fun save(onDone: () -> Unit) {
        Log.d("TRIP_DETAIL_SAVE", "save() called, hasUnsavedChanges=${_uiState.value.hasUnsavedChanges}")
        val current = _uiState.value
        if (current.isSaving) return

        // 1) propsat rozeditované věci do state (aby se neztratil poslední znak)
        commitAllEditingBeforeSave()

        val state = _uiState.value
        if (!state.hasUnsavedChanges) {
            onDone()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            runCatching {
                val dto = buildUpdateDto(_uiState.value)
                Log.d("TRIP_DETAIL_SAVE", "PATCH dto=$dto")
                tripsRepository.updateTripDetail(tripId, dto)
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = false) }
                loadTrip()
                onDone()
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    private fun buildUpdateDto(state: TripDetailUiState): TripDetailUpdateDto {
        val checklistDtos = state.checklistItems
            .map { it.copy(text = it.text.trim()) }
            .filter { it.text.isNotBlank() }
            .mapIndexed { idx, i ->
                TripDetailUpdateDto.ChecklistItemDto(
                    text = i.text,
                    checked = i.checked,
                    order = idx
                )
            }

        val noteDtos = state.notes
            .map { it.copy(text = it.text.trim()) }
            .filter { it.text.isNotBlank() }
            .mapIndexed { idx, n ->
                TripDetailUpdateDto.NoteDto(
                    text = n.text,
                    order = idx
                )
            }

        val tipsDtos = state.tipsAndTripsItems
            .map { it.copy(title = it.title.trim()) }
            .filter { it.title.isNotBlank() }
            .mapIndexed { idx, t ->
                TripDetailUpdateDto.TipAndTripDto(
                    title = t.title,
                    imageUrl = t.imageUri?.toString(),
                    order = idx
                )
            }

        return TripDetailUpdateDto(
            from = state.fromText.trim().takeIf { it.isNotBlank() },
            to = state.toText.trim().takeIf { it.isNotBlank() },
            transport = state.transport.firstOrNull()?.name?.lowercase(),
            theme = state.selectedTheme?.name?.lowercase(),
            coverImageUrl = state.coverImageUrl,
            mapImageUrl = state.mapImageUrl,
            mapImageFullUrl = state.mapFullImageUrl,
            plannedBudget = state.plannedBudget.trim().takeIf { it.isNotBlank() },
            spentBudget = state.spentBudget.trim().takeIf { it.isNotBlank() },

            // ✅ vždy posíláme
            checklistItems = checklistDtos,
            notes = noteDtos,
            tipsAndTrips = tipsDtos
        )
    }

    fun onAddMemberClick() {
        // TODO
    }

    fun onDeleteTripClick(onDone: () -> Unit) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            runCatching {
                tripsRepository.deleteTrip(tripId)
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                onDone()
            }.onFailure { e ->
                Log.e("TRIP_DETAIL_DELETE", "delete failed", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
}
