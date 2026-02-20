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
                    mapFullImageUrl = trip.mapImageFullUrl ?: trip.mapImageUrl,
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
                return@update state.copy(
                    checklistItems = items,
                    editingChecklistIndex = null,
                    editingChecklistText = TextFieldValue("")   // ✅
                )
            }

            if (index in items.indices) {
                items[index] = items[index].copy(text = text)
            }

            state.copy(
                checklistItems = items,
                editingChecklistIndex = null,
                editingChecklistText = TextFieldValue("")     // ✅
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
            state.copy(checklistItems = items)
        }
    }

    fun removeChecklistItem(index: Int) {
        _uiState.update { state ->
            val items = state.checklistItems.toMutableList()
            if (index in items.indices) items.removeAt(index)
            state.copy(checklistItems = items)
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
                return@update state.copy(
                    notes = items,
                    editingNoteIndex = null,
                    editingNoteText = TextFieldValue("")
                )
            }

            if (index in items.indices) {
                items[index] = items[index].copy(text = text)
            }

            state.copy(
                notes = items,
                editingNoteIndex = null,
                editingNoteText = TextFieldValue("")
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
            state.copy(notes = items)
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
                editingBudgetText = TextFieldValue("")
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
            if (index !in items.indices) {
                return@update state.copy(
                    editingTipsIndex = null,
                    editingTipsText = TextFieldValue(""),
                    isTipsAndTripsAdding = false,
                    pickingTipsPhotoIndex = null
                )
            }

            if (text.isBlank()) {
                items.removeAt(index)
            } else {
                items[index] = items[index].copy(title = text)
            }

            state.copy(
                tipsAndTripsItems = items,
                isTipsAndTripsAdding = false,
                editingTipsIndex = null,
                editingTipsText = TextFieldValue(""),
                pickingTipsPhotoIndex = null
            )
        }
    }

    fun removeTipsAndTripsItem(index: Int) {
        _uiState.update { state ->
            val items = state.tipsAndTripsItems.toMutableList()
            if (index !in items.indices) return@update state

            val editingIndex = state.editingTipsIndex
            val pickingIndex = state.pickingTipsPhotoIndex

            items.removeAt(index)

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
                pickingTipsPhotoIndex = null
            )
        }
    }






    fun onAddMemberClick() {
        // TODO
    }

    fun onDeleteTripClick() {
        // TODO
    }
}
