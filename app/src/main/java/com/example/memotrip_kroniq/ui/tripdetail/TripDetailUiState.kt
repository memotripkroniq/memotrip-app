package com.example.memotrip_kroniq.ui.tripdetail

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import com.example.memotrip_kroniq.ui.core.model.ThemeType
import com.example.memotrip_kroniq.ui.core.model.TransportType
import com.example.memotrip_kroniq.ui.tripdetail.components.BudgetEditField
import com.example.memotrip_kroniq.ui.tripdetail.components.ChecklistItemUi
import com.example.memotrip_kroniq.ui.tripdetail.components.NoteItemUi
import com.example.memotrip_kroniq.ui.tripdetail.components.TripMemberUi
import com.example.memotrip_kroniq.ui.tripdetail.components.TripPhotoCategoryUi
import com.example.memotrip_kroniq.ui.tripdetail.components.TripPhotoUi
import com.example.memotrip_kroniq.ui.tripdetail.components.ThemeUi
import com.example.memotrip_kroniq.ui.tripdetail.components.TipsAndTripsItemUi

data class TripDetailUiState(
    // --- Saving / dirty ---
    val isInitialLoading: Boolean = true,
    val isHeroLoading: Boolean = true,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val errorMessage: String? = null,

    // Cover + maps
    val coverImageUrl: String? = null,
    val localCoverPhotoUri: Uri? = null,
    val mapImageUrl: String? = null,
    val mapFullImageUrl: String? = null,
    val tripName: String = "",

    // Tabs
    val selectedTab: TripDetailTab = TripDetailTab.DETAILS,
    val isPhotosLoading: Boolean = false,
    val photoCategories: List<TripPhotoCategoryUi> = emptyList(),
    val tripPhotos: List<TripPhotoUi> = emptyList(),

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
    val isTipsAndTripsAdding: Boolean = false,
    val editingTipsIndex: Int? = null,
    val editingTipsText: TextFieldValue = TextFieldValue(""),
    val pickingTipsPhotoIndex: Int? = null,

    ) {
    // Derived state
    val isKroniqLocked: Boolean get() = !hasKroniqPackage
}
