package com.example.memotrip_kroniq.ui.tripdetail

import androidx.compose.ui.text.input.TextFieldValue
import com.example.memotrip_kroniq.ui.core.model.ThemeType
import com.example.memotrip_kroniq.ui.core.model.TransportType
import com.example.memotrip_kroniq.ui.tripdetail.components.BudgetEditField
import com.example.memotrip_kroniq.ui.tripdetail.components.ChecklistItemUi
import com.example.memotrip_kroniq.ui.tripdetail.components.NoteItemUi
import com.example.memotrip_kroniq.ui.tripdetail.components.TripMemberUi
import com.example.memotrip_kroniq.ui.tripdetail.components.ThemeUi
import com.example.memotrip_kroniq.ui.tripdetail.components.TipsAndTripsItemUi

data class TripDetailUiState(
    // Cover + maps
    val coverImageUrl: String? = null,
    val mapImageUrl: String? = null,
    val mapFullImageUrl: String? = null,

    // Tabs
    val selectedTab: TripDetailTab = TripDetailTab.DETAILS,

    // Members
    val members: List<TripMemberUi> = emptyList(),

    // KroniQ
    val hasKroniqPackage: Boolean = false,
    val isSharedInKroniq: Boolean = false,

    // Themes
    val themes: List<ThemeUi> = emptyList(),
    val selectedTheme: ThemeType? = null,
    val isThemesLocked: Boolean = true,

    // Trip info
    val tripDateText: String = "",
    val fromText: String = "",
    val toText: String = "",
    val transport: Set<TransportType> = emptySet(),

    // Checklist
    val checklistItems: List<ChecklistItemUi> = emptyList(),
    val editingChecklistIndex: Int? = null,
    val editingChecklistText: TextFieldValue = TextFieldValue(""),


    // Notes
    val notes: List<NoteItemUi> = emptyList(),
    val editingNoteIndex: Int? = null,
    val editingNoteText: TextFieldValue = TextFieldValue(""),


    //Budget
    val plannedBudget: String = "",
    val spentBudget: String = "",
    val isBudgetVisible: Boolean = true,
    val editingBudgetField: BudgetEditField? = null,
    val editingBudgetText: TextFieldValue = TextFieldValue(""),

    // Tips & Trips
    val tipsAndTripsItems: List<TipsAndTripsItemUi> = emptyList(),
    val isTipsAndTripsAdding: Boolean = false

) {
    // Derived state
    val isKroniqLocked: Boolean get() = !hasKroniqPackage
}
