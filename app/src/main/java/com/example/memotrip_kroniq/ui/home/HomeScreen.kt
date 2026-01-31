package com.example.memotrip_kroniq.ui.home

import PreviewUiScaler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.data.trips.TripsRepository
import com.example.memotrip_kroniq.navigation.Screen
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.components.*
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController? = null,
    initialTab: HomeTab = HomeTab.THEMES
) {
    val s = LocalUiScaler.current
    var selectedTab by remember { mutableStateOf(initialTab) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry = navController?.currentBackStackEntryAsState()

    val tokenStore = remember { TokenDataStore(context) }
    RetrofitClient.build(tokenStore)



    //// 🔐 JEDEN TokenDataStore
    //val tokenStore = remember { TokenDataStore(context) }

    // 🔌 Repositories až POTOM
    val authRepository = remember {
        AuthRepository(
            api = RetrofitClient.authApi,
            tokenStore = tokenStore
        )
    }

    val tripsRepository = remember {
        TripsRepository(
            api = RetrofitClient.tripsApi,
            contentResolver = context.contentResolver
        )
    }

    // 🔹 ViewModel – lifecycle-safe
    val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    authRepository = authRepository,
                    tripsRepository = tripsRepository
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    fun logout() {
        coroutineScope.launch {
            tokenStore.clearTokens()
            navController?.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    // 🔥🔥🔥 ZMĚNA #1 – Scaffold přebírá layout a top bar
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            AppTopBar(
                title = "Add Trip",
                showBack = false,
                onMenuClick = ::logout
            )
        }
    ) { innerPadding ->

        // 🔥🔥🔥 ZMĚNA #2 – HomeContent je čistý obsah
        HomeContent(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16f.sx(s)),
            selectedTab = selectedTab,
            isThemesLocked = uiState.isThemesLocked,
            trips = uiState.trips,
            isTripsLoading = uiState.isTripsLoading,
            isKroniq = uiState.isKroniq,
            onTabSelected = { selectedTab = it },
            onAddTripClick = {
                navController?.navigate(Screen.AddTrip.route)
            }
        )

    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun HomeScreenPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            HomeScreen(
                navController = null,
                initialTab = HomeTab.TRIP_HISTORY
            )
        }
    }
}

