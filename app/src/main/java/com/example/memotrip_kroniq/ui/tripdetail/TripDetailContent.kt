package com.example.memotrip_kroniq.ui.tripdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.model.ThemeType
import com.example.memotrip_kroniq.ui.core.model.TransportType
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.tripdetail.components.*
import PreviewUiScaler
import android.net.Uri
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.TextFieldValue
import com.example.memotrip_kroniq.ui.addtrip.components.ThemeSelector
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.tripdetail.components.ZoomableImageDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts




@Composable
fun TripDetailContent(
    modifier: Modifier = Modifier,
    uiState: TripDetailUiState,
    onTabSelected: (TripDetailTab) -> Unit,
    onAddMemberClick: () -> Unit,
    onDeleteTripClick: () -> Unit,
    onAddChecklistItem: () -> Unit,
    onToggleChecklistItem: (Int) -> Unit,
    onRemoveChecklistItem: (Int) -> Unit,
    onStartEditChecklistItem: (Int) -> Unit,
    onEditingChecklistTextChange: (TextFieldValue) -> Unit,
    onCommitEditChecklistItem: () -> Unit,
    onCancelEditChecklistItem: () -> Unit,
    onAddNoteItem: () -> Unit,
    onRemoveNoteItem: (Int) -> Unit,
    onStartEditNoteItem: (Int) -> Unit,
    onEditingNoteTextChange: (TextFieldValue) -> Unit,
    onCommitEditNoteItem: () -> Unit,
    onCancelEditNoteItem: () -> Unit,
    onToggleBudgetVisibility: () -> Unit,
    onStartEditBudget: (BudgetEditField) -> Unit,
    onEditingBudgetTextChange: (TextFieldValue) -> Unit,
    onCommitEditBudget: () -> Unit,
    onTipsAddClick: () -> Unit,
    onTipsCancelAddClick: () -> Unit,
    onTipsRemoveItem: (Int) -> Unit,

    onStartEditTipsItem: (Int) -> Unit,
    onEditingTipsTextChange: (TextFieldValue) -> Unit,
    onCommitEditTipsItem: () -> Unit,

    onTipsRequestPickPhoto: (Int) -> Unit,
    onTipsPhotoPicked: (Uri?) -> Unit,


    ) {
    val s = LocalUiScaler.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        item {
            var showMap by remember { mutableStateOf(false) }

            TripHeroSection(
                coverUrl = uiState.coverImageUrl,
                mapUrl = uiState.mapImageUrl,
                onChangeCoverClick = { /* ... */ },
                onMapClick = {
                    if (!uiState.mapImageUrl.isNullOrBlank()) showMap = true
                }
            )

            val fullUrl = uiState.mapFullImageUrl

            if (showMap && !fullUrl.isNullOrBlank()) {
                ZoomableImageDialog(
                    imageUrl = fullUrl,
                    onDismiss = { showMap = false }
                )
            }

            Spacer(Modifier.height(16f.sy(s)))
        }

        item {
            TripTabs(
                selectedTab = uiState.selectedTab,
                onTabSelected = onTabSelected
            )
            Spacer(Modifier.height(20f.sy(s)))
        }

        item {
            TripMembersSection(
                members = uiState.members,
                onAddMemberClick = onAddMemberClick
            )
            Spacer(Modifier.height(20f.sy(s)))
        }


        item {
            ShareInKroniqSection(
                checked = uiState.isSharedInKroniq,
                locked = uiState.isKroniqLocked, // nebo !uiState.hasKroniqPackage
                onToggle = { /* zatím empty */ }
            )
            Spacer(Modifier.height(20f.sy(s)))
        }

        item {
            ThemeSelector(
                selected = uiState.selectedTheme,
                locked = uiState.isThemesLocked,
                onSelect = { /* zatím */ }
            )
            Spacer(Modifier.height(20f.sy(s)))
        }

        item {
            TripInfoCard(
                dateText = uiState.tripDateText,
                fromText = uiState.fromText,
                toText = uiState.toText,
                transport = uiState.transport,
                onEditClick = { /* zatím nic */ },
                theme = uiState.selectedTheme
            )
            Spacer(Modifier.height(20f.sy(s)))
        }

        item {
            ChecklistCard(
                items = uiState.checklistItems,
                editingIndex = uiState.editingChecklistIndex,
                editingText = uiState.editingChecklistText, // TextFieldValue

                onAddClick = onAddChecklistItem,
                onToggleChecked = onToggleChecklistItem,
                onRemoveItem = onRemoveChecklistItem,

                onStartEdit = onStartEditChecklistItem,
                onEditingTextChange = onEditingChecklistTextChange, // TextFieldValue -> Unit
                onCommitEdit = onCommitEditChecklistItem,
                onCancelEdit = onCancelEditChecklistItem
            )

            Spacer(Modifier.height(16f.sy(s)))
        }


        item {
            NotesCard(
                items = uiState.notes,
                onAddClick = onAddNoteItem,
                onRemoveItem = { index -> onRemoveNoteItem(index) },

                editingIndex = uiState.editingNoteIndex,
                editingText = uiState.editingNoteText,
                onStartEdit = { index -> onStartEditNoteItem(index) },
                onEditingTextChange = onEditingNoteTextChange,
                onCommitEdit = onCommitEditNoteItem,
                onCancelEdit = onCancelEditNoteItem
            )
            Spacer(Modifier.height(16f.sy(s)))
        }




        item {
            BudgetCard(
                plannedAmount = uiState.plannedBudget,
                spentAmount = uiState.spentBudget,
                isVisible = uiState.isBudgetVisible,
                onToggleVisibility = onToggleBudgetVisibility,

                editingField = uiState.editingBudgetField,
                editingText = uiState.editingBudgetText,
                onStartEdit = onStartEditBudget,
                onEditingTextChange = onEditingBudgetTextChange,
                onCommitEdit = onCommitEditBudget
            )

            Spacer(Modifier.height(16f.sy(s)))
        }


        item {
            // ✅ picker pro fotku
            val tipsImagePicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                onTipsPhotoPicked(uri)
            }

            TipsAndTripsCard(
                items = uiState.tipsAndTripsItems,
                isAdding = uiState.isTipsAndTripsAdding,

                // ✅ inline edit (Keep-like)
                editingIndex = uiState.editingTipsIndex,
                editingText = uiState.editingTipsText,
                onStartEdit = onStartEditTipsItem,
                onEditingTextChange = onEditingTipsTextChange,
                onCommitEdit = onCommitEditTipsItem,

                // ✅ akce
                onAddClick = onTipsAddClick,
                onCancelAddClick = onTipsCancelAddClick,
                onPickImageClick = {
                    // pokud chceš použít pro "aktuálně editovaný" item
                    val idx = uiState.editingTipsIndex
                        ?: uiState.tipsAndTripsItems.lastIndex.takeIf { it >= 0 }
                        ?: return@TipsAndTripsCard

                    onTipsRequestPickPhoto(idx)
                    tipsImagePicker.launch("image/*")
                },
                onAddItemPhotoClick = { index ->
                    onTipsRequestPickPhoto(index)
                    tipsImagePicker.launch("image/*")
                },
                onRemoveItem = onTipsRemoveItem
            )

            Spacer(Modifier.height(24f.sy(s)))
        }


        item {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryButton(
                    text = "Delete trip",
                    onClick = onDeleteTripClick,
                    modifier = Modifier
                        .width(200f.sx(s))
                        .align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(24f.sy(s)))
        }
    }
}

@Preview(
    name = "TripDetailContent",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 1800
)
@Composable
private fun TripDetailContentPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            TripDetailContent(
                uiState = TripDetailUiState(
                    members = listOf(
                        TripMemberUi("1", "Kristin", avatarRes = R.drawable.some_avatar_kristin),
                        TripMemberUi("2", "Peetr", avatarRes = R.drawable.some_avatar_peetr),
                        TripMemberUi("3", "Lily", avatarRes = R.drawable.some_avatar_lily),
                        TripMemberUi("4", "Sara", avatarRes = R.drawable.some_avatar_sara)
                    ),
                    themes = listOf(
                        ThemeUi(ThemeType.SUMMER, R.drawable.homescreen_theme_summer),
                        ThemeUi(ThemeType.WINTER, R.drawable.homescreen_theme_winter),
                        ThemeUi(ThemeType.CAMPING, R.drawable.homescreen_theme_camping),
                        ThemeUi(ThemeType.CITIES, R.drawable.homescreen_theme_cities),
                        ThemeUi(ThemeType.NATURE, R.drawable.homescreen_theme_nature)
                    ),
                    selectedTheme = ThemeType.SUMMER,
                    isThemesLocked = false,

                    tripDateText = "28 June 2025 - 11 July 2025",
                    fromText = "Slovenský Grob, Slovakia",
                    toText = "Camping Lacona Pineta, Elba",
                    transport = setOf(TransportType.PLANE, TransportType.CAR),
                    checklistItems = listOf(
                        ChecklistItemUi("Passport", checked = true),
                        ChecklistItemUi("Powerbank", checked = false)
                    ),
                    notes = listOf(
                        NoteItemUi("Buy groceries before trip"),
                        NoteItemUi("Check ferry times")
                    ),
                    plannedBudget = "1 200 €",
                    spentBudget = "340 €",
                    isBudgetVisible = true,
                    tipsAndTripsItems = listOf(
                        TipsAndTripsItemUi(title = "Hidden beach"),
                        TipsAndTripsItemUi(title = "Try local market")
                    ),
                    hasKroniqPackage = false,
                    isSharedInKroniq = false
                ),
                onTabSelected = {},
                onAddMemberClick = {},
                onAddChecklistItem = {},
                onToggleChecklistItem = {},
                onStartEditChecklistItem = {},
                onEditingChecklistTextChange = {},
                onCommitEditChecklistItem = {},
                onCancelEditChecklistItem = {},
                onRemoveChecklistItem = {},
                onDeleteTripClick = {},
                onAddNoteItem = {},
                onRemoveNoteItem = {},
                onStartEditNoteItem = {},
                onEditingNoteTextChange = {},
                onCommitEditNoteItem = {},
                onCancelEditNoteItem = {},
                onToggleBudgetVisibility = {},
                onStartEditBudget = { _: BudgetEditField -> },
                onEditingBudgetTextChange = { _: TextFieldValue -> },
                onCommitEditBudget = {},
                onTipsAddClick = {},
                onTipsCancelAddClick = {},
                onTipsRemoveItem = {},
                onStartEditTipsItem = {},
                onEditingTipsTextChange = {},
                onCommitEditTipsItem = {},
                onTipsRequestPickPhoto = {},
                onTipsPhotoPicked = {},


                )
        }
    }
}


