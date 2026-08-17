package com.example.memotrip_kroniq.ui.home

import PreviewUiScaler
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.payments.PaymentsRepository
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.data.trips.TripsRepository
import com.example.memotrip_kroniq.navigation.Screen
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.home.payments.launchDummyPayUrl
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@kotlin.OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController? = null,
    initialTab: HomeTab = HomeTab.THEMES
) {
    val s = LocalUiScaler.current
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }

    val context = LocalContext.current

    val currentBackStackEntry = navController?.currentBackStackEntryAsState()?.value
    val savedStateHandle = currentBackStackEntry?.savedStateHandle

    val tokenStore = remember { TokenDataStore(context) }

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

    val paymentsRepository = remember {
        PaymentsRepository(
            api = RetrofitClient.paymentsApi
        )
    }

    // 🔹 ViewModel – lifecycle-safe
    val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    authRepository = authRepository,
                    tripsRepository = tripsRepository,
                    paymentsRepository = paymentsRepository
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.dummyPayUrlToOpen) {
        val paymentUrl = uiState.dummyPayUrlToOpen ?: return@LaunchedEffect

        runCatching {
            launchDummyPayUrl(context, paymentUrl)
            viewModel.onDummyPayBrowserOpened()
        }.onFailure {
            Toast.makeText(
                context,
                it.message ?: "Unable to open payment page.",
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.consumeDummyPayUrl()
    }

    LaunchedEffect(uiState.dummyPayErrorMessage) {
        val errorMessage = uiState.dummyPayErrorMessage ?: return@LaunchedEffect
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        viewModel.consumeDummyPayError()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var hasHandledInitialResume by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (hasHandledInitialResume) {
                    viewModel.refreshHomeData()
                    viewModel.refreshUserFromPaymentReturnIfNeeded()
                } else {
                    hasHandledInitialResume = true
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val showThemesBack = selectedTab == HomeTab.THEMES &&
            uiState.themesContentState is ThemesContentState.ThemeTrips

    LaunchedEffect(savedStateHandle) {
        val deleted = savedStateHandle?.get<Boolean>("trip_deleted") ?: false
        if (deleted) {
            savedStateHandle["trip_deleted"] = false
            selectedTab = HomeTab.TRIP_HISTORY
        }

        val updated = savedStateHandle?.get<Boolean>("trip_updated") ?: false
        if (updated) {
            savedStateHandle["trip_updated"] = false
        }
    }


    // 🔥🔥🔥 ZMĚNA #1 – Scaffold přebírá layout a top bar
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.home_title),
                showBack = showThemesBack,
                onBackClick = { viewModel.onThemesBackClick() },
                onMenuClick = {
                    navController?.navigate(Screen.Settings.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { innerPadding ->
        Crossfade(
            targetState = uiState.isLoading,
            animationSpec = tween(durationMillis = 220),
            label = "home_loading_transition"
        ) { isLoading ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.Black),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                HomeContent(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 16f.sx(s)),
                    selectedTab = selectedTab,
                    isHomeLoading = uiState.isLoading,
                    isThemesLocked = uiState.isThemesLocked,
                    trips = uiState.trips,
                    themeTrips = viewModel.getTripsForSelectedTheme(),
                    themesContentState = uiState.themesContentState,
                    isTripsLoading = uiState.isTripsLoading,
                    isRefreshingHome = uiState.isRefreshingHome,
                    isCheckingAddTripAccess = uiState.isCheckingAddTripAccess,
                    isPremium = uiState.isPremium,
                    isKroniq = uiState.isKroniq,
                    isAddTripEnabled = uiState.isAddTripEnabled,
                    onTabSelected = { selectedTab = it },
                    onThemeClick = viewModel::onThemeClick,
                    onThemesBackClick = viewModel::onThemesBackClick,
                    onAddTripClick = {
                        viewModel.verifyAddTripAccess(
                            onAllowed = {
                                navController?.navigate(Screen.AddTrip.route)
                            },
                            onBlocked = {
                                selectedTab = HomeTab.TRIP_HISTORY
                            }
                        )
                    },
                    onTripHistoryRefresh = viewModel::refreshHomeData,
                    onTripClick = { tripId -> navController?.navigate(Screen.TripDetail.createRoute(tripId)) },
                    onGetPremiumClick = viewModel::startPremiumDummyPay,
                    onGetKroniqClick = viewModel::startKroniqDummyPay,
                    dummyPayLoadingPlan = uiState.dummyPayLoadingPlan
                )
            }
        }
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
                initialTab = HomeTab.TRIP_HISTORY,
            )
        }
    }
}

