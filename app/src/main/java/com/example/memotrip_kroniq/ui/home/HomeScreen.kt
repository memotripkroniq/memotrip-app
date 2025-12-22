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
fun HomeScreen(
    navController: NavHostController? = null,
    initialTab: HomeTab = HomeTab.THEMES
) {
    val s = LocalUiScaler.current
    var selectedTab by remember { mutableStateOf(initialTab) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 🔐 JEDEN TokenDataStore (STEJNÝ VZOR)
    val tokenStore = remember { TokenDataStore(context) }

    // 🔌 Repository
    val repository = remember {
        AuthRepository(
            api = RetrofitClient.api,
            tokenStore = tokenStore
        )
    }

    // 🔹 ViewModel – lifecycle-safe
    val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    authRepository = repository
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

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
                .padding(innerPadding)           // 🔥 respektuje topBar
                .padding(horizontal = 16f.sx(s)), // 🔥 původní padding
            selectedTab = selectedTab,
            isThemesLocked = uiState.isThemesLocked,
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

            // ⚠️ NavController v preview NEPOTŘEBUJEME
            HomeScreen(
                navController = null,
                initialTab = HomeTab.THEMES
                // zkus klidně i:
                // initialTab = HomeTab.TRIP_HISTORY
            )
        }
    }
}

