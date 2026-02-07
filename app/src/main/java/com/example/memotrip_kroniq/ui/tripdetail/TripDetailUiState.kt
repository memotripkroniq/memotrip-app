package com.example.memotrip_kroniq.ui.tripdetail

import com.example.memotrip_kroniq.ui.addtrip.ThemeType
import com.example.memotrip_kroniq.ui.addtrip.TransportType
import com.example.memotrip_kroniq.ui.tripdetail.components.BudgetUi
import com.example.memotrip_kroniq.ui.tripdetail.components.ChecklistItemUi
import com.example.memotrip_kroniq.ui.tripdetail.components.NoteItemUi
import com.example.memotrip_kroniq.ui.tripdetail.components.TripMemberUi
import com.example.memotrip_kroniq.ui.tripdetail.components.ThemeUi
import com.example.memotrip_kroniq.ui.tripdetail.components.TipsAndTripsItemUi

data class TripDetailUiState(
    // Cover + maps
    val coverImageUrl: String? = null,
    val mapImageUrl: String? = null,

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

    // Notes
    val notes: List<NoteItemUi> = emptyList(),

    //Budget
    val plannedBudget: String = "",
    val spentBudget: String = "",
    val isBudgetVisible: Boolean = true, // TODO: Zatím neřešíme

    // Tips & Trips
    val tipsAndTripsItems: List<TipsAndTripsItemUi> = emptyList(),
    val isTipsAndTripsAdding: Boolean = false

) {
    // Derived state
    val isKroniqLocked: Boolean get() = !hasKroniqPackage
}
