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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import com.example.memotrip_kroniq.ui.addtrip.components.ThemeSelector
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme


@Composable
fun TripDetailContent(
    modifier: Modifier = Modifier,
    uiState: TripDetailUiState,
    onTabSelected: (TripDetailTab) -> Unit,
    onAddMemberClick: () -> Unit,
    onDeleteTripClick: () -> Unit
) {
    val s = LocalUiScaler.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        item {
            TripHeroSection(
                coverUrl = uiState.coverImageUrl,
                mapUrl = uiState.mapImageUrl,
                onChangeCoverClick = { /* UI only */ }
            )
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
                onAddClick = {
                    // TODO: otevřít input / přidat nový item
                },
                onToggleChecked = { index ->
                    // TODO: toggle checked u položky index
                },
                onRemoveItem = { index ->
                    // TODO: odstranit položku index
                }
            )
            Spacer(Modifier.height(16f.sy(s)))
        }

        item {
            NotesCard(
                items = uiState.notes,
                onAddClick = { /* TODO */ },
                onRemoveItem = { /* TODO */ }
            )
            Spacer(Modifier.height(16f.sy(s)))
        }


        item {
            BudgetCard(
                plannedAmount = uiState.plannedBudget,
                spentAmount = uiState.spentBudget,
                isVisible = uiState.isBudgetVisible,
                onToggleVisibility = { /* zatím prázdné – napojíme později */ }
            )
            Spacer(Modifier.height(16f.sy(s)))
        }


        item {
            TipsAndTripsCard(
                items = uiState.tipsAndTripsItems,
                isAdding = uiState.isTipsAndTripsAdding,
                onAddClick = { /* state.isTipsAndTripsAdding = true */ },
                onCancelAddClick = { /* state.isTipsAndTripsAdding = false */ },
                onPickImageClick = { /* UI only */ },
                onAddItemPhotoClick = { index -> /* UI only */ },
                onRemoveItem = { index -> /* UI only */ }
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
                onDeleteTripClick = {}
            )
        }
    }
}


