package com.example.memotrip_kroniq.ui.home

import androidx.compose.runtime.Composable
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import PreviewUiScaler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.navigation.Screen
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.components.*
import com.example.memotrip_kroniq.ui.home.model.TripHistoryItem
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    selectedTab: HomeTab,
    isThemesLocked: Boolean,
    trips: List<TripHistoryItem>,
    themeTrips: List<TripHistoryItem>,
    themesContentState: ThemesContentState,
    isTripsLoading: Boolean,
    isKroniq: Boolean,
    isAddTripEnabled: Boolean,
    onTabSelected: (HomeTab) -> Unit,
    onThemeClick: (String) -> Unit,
    onThemesBackClick: () -> Unit,
    onAddTripClick: () -> Unit,
    onTripClick: (String) -> Unit
) {
    val s = LocalUiScaler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        HeroBanner(
            onAddTripClick = onAddTripClick,
            isAddTripEnabled = isAddTripEnabled
        )

        Spacer(modifier = Modifier.height(13f.sy(s)))

        HomeTabs(
            selected = selectedTab,
            isThemesLocked = isThemesLocked,
            onToggleLock = {},
            onTabSelected = onTabSelected
        )

        Spacer(modifier = Modifier.height(13f.sy(s)))

        when (selectedTab) {
            HomeTab.THEMES -> {
                when (val contentState = themesContentState) {
                    ThemesContentState.Grid -> {
                        ThemesGrid(
                            locked = isThemesLocked,
                            onThemeClick = onThemeClick
                        )
                    }

                    is ThemesContentState.ThemeTrips -> {
                        android.util.Log.d("HOME_THEME", "render ThemeTrips for ${contentState.theme}")

                        when {
                            isTripsLoading -> {
                                TripHistoryLoadingContent()
                            }
                            themeTrips.isEmpty() -> {
                                TripHistoryEmptyContent()
                            }
                            else -> {
                                TripHistoryList(
                                    trips = themeTrips,
                                    showUpsell = false,
                                    onTripClick = onTripClick
                                )
                            }
                        }
                    }
                }
            }
            HomeTab.TRIP_HISTORY -> {
                when {
                    isTripsLoading -> {
                        TripHistoryLoadingContent() // klidně jen loader
                    }
                    trips.isEmpty() -> {
                        TripHistoryEmptyContent()
                    }
                    else -> {
                        TripHistoryList(
                            trips = trips,
                            showUpsell = !isKroniq,
                            onTripClick = onTripClick
                        )
                    }
                }
            }
        }
    }
}


@Preview(
    showBackground = true,
    widthDp = 412,
    heightDp = 892
)
@Composable
fun HomeContentPreview_TripHistory() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            HomeContent(
                selectedTab = HomeTab.TRIP_HISTORY,
                isThemesLocked = true,
                trips = listOf(
                    TripHistoryItem(
                        id = "1",
                        title = "Trip to Italy",
                        coverImageUrl = null,
                        theme = "Summer"
                    ),
                    TripHistoryItem(
                        id = "2",
                        title = "Skiing Alps",
                        coverImageUrl = "https://picsum.photos/400/200",
                        theme = "Winter"
                    )
                ),
                themeTrips = emptyList(),
                themesContentState = ThemesContentState.Grid,
                isTripsLoading = false,
                onTabSelected = {},
                onThemeClick = {},
                onThemesBackClick = {},
                onAddTripClick = {},
                isKroniq = false,
                isAddTripEnabled = true,
                onTripClick = {}
            )
        }
    }
}

