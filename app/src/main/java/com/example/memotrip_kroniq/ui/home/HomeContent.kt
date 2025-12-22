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
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    selectedTab: HomeTab,
    isThemesLocked: Boolean,
    onTabSelected: (HomeTab) -> Unit,
    onAddTripClick: () -> Unit
) {
    val s = LocalUiScaler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        HeroBanner(
            onAddTripClick = onAddTripClick
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
                ThemesGrid(locked = isThemesLocked)
            }
            HomeTab.TRIP_HISTORY -> {
                TripHistoryEmptyContent()
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
                selectedTab = HomeTab.THEMES,
                isThemesLocked = false,
                onTabSelected = {},
                onAddTripClick = {}
            )
        }
    }
}

