package com.example.memotrip_kroniq.ui.addtrip

import PreviewUiScaler
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.location.LocationSearchRepository
import com.example.memotrip_kroniq.data.location.LocationSuggestion
import com.example.memotrip_kroniq.data.network.HttpClientProvider
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.data.tripmap.RemoteTripMapGenerator
import com.example.memotrip_kroniq.data.tripmap.TripMapGenerator
import com.example.memotrip_kroniq.data.trips.TripsRepository
import com.example.memotrip_kroniq.navigation.*
import com.example.memotrip_kroniq.ui.addtrip.components.AddTripDatePickerOverlay
import com.example.memotrip_kroniq.ui.addtrip.screens.SavingTripScreen
import com.example.memotrip_kroniq.ui.addtrip.screens.TripSuccessScreen
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.home.HomeScreen
import com.example.memotrip_kroniq.ui.home.HomeTab
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import kotlinx.coroutines.flow.MutableStateFlow

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTripScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val s = LocalUiScaler.current
    val focusManager = LocalFocusManager.current

    // ─────────────────────────────
    // Token store + Retrofit init (MUSÍ být před api get())
    // ─────────────────────────────
    val tokenStore = remember(context) { TokenDataStore(context) }

    // Build retrofit přesně jednou
    remember(tokenStore) {
        RetrofitClient.build(tokenStore)
        true
    }

    // ─────────────────────────────
    // Dependencies
    // ─────────────────────────────
    val tripsRepository = remember(context) {
        TripsRepository(
            api = RetrofitClient.tripsApi,
            contentResolver = context.contentResolver
        )
    }

    val authRepository = remember(tokenStore) {
        AuthRepository(
            api = RetrofitClient.authApi,
            tokenStore = tokenStore
        )
    }

    val locationSearchRepository = remember {
        LocationSearchRepository(
            client = HttpClientProvider.client
        )
    }

    val tripMapGenerator: TripMapGenerator = remember {
        RemoteTripMapGenerator(
            client = HttpClientProvider.client
        )
    }

    val factory = remember(tripsRepository, authRepository, locationSearchRepository, tripMapGenerator) {
        AddTripViewModelFactory(
            tripsRepository = tripsRepository,
            authRepository = authRepository,
            locationSearchRepository = locationSearchRepository,
            tripMapGenerator = tripMapGenerator
        )
    }

    val viewModel: AddTripViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    // ─────────────────────────────
    // SavedStateHandle (LocationSearch)
    // ─────────────────────────────
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val handle = currentBackStackEntry?.savedStateHandle

    val locationName by remember(handle) {
        handle?.getStateFlow<String?>(LOCATION_NAME_KEY, null) ?: MutableStateFlow(null)
    }.collectAsState()

    val locationLat by remember(handle) {
        handle?.getStateFlow<Double?>(LOCATION_LAT_KEY, null) ?: MutableStateFlow(null)
    }.collectAsState()

    val locationLon by remember(handle) {
        handle?.getStateFlow<Double?>(LOCATION_LON_KEY, null) ?: MutableStateFlow(null)
    }.collectAsState()

    val targetName by remember(handle) {
        handle?.getStateFlow<String?>(LOCATION_TARGET_KEY, null) ?: MutableStateFlow(null)
    }.collectAsState()

    val stopIndex by remember(handle) {
        handle?.getStateFlow<Int?>(LOCATION_RESULT_KEY, null) ?: MutableStateFlow(null)
    }.collectAsState()

    // Apply selected location back to ViewModel
    LaunchedEffect(locationName, locationLat, locationLon, targetName, stopIndex) {
        if (locationName == null || locationLat == null || locationLon == null) return@LaunchedEffect
        if (targetName == null) return@LaunchedEffect

        val suggestion = LocationSuggestion(
            displayName = locationName!!,
            lat = locationLat!!,
            lon = locationLon!!
        )

        when (targetName) {
            LocationTarget.FROM.name -> viewModel.onFromSuggestionSelected(suggestion)
            LocationTarget.TO.name -> viewModel.onToSuggestionSelected(suggestion)
            LocationTarget.STOP.name -> {
                val index = stopIndex ?: return@LaunchedEffect
                viewModel.onStopSuggestionSelected(index, suggestion)
            }
        }

        handle?.apply {
            remove<String>(LOCATION_NAME_KEY)
            remove<Double>(LOCATION_LAT_KEY)
            remove<Double>(LOCATION_LON_KEY)
            remove<String>(LOCATION_TARGET_KEY)
            remove<Int>(LOCATION_RESULT_KEY)
        }
    }

    // ─────────────────────────────
    // Root layout
    // ─────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        Scaffold(
            containerColor = Color.Black,
            topBar = {
                AppTopBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = "Add Trip",
                    showBack = uiState.flowState == AddTripFlowState.IDLE,
                    onBackClick = { navController.popBackStack() }
                )
            }
        ) { innerPadding ->

            when (uiState.flowState) {
                AddTripFlowState.SAVING -> SavingTripScreen()
                AddTripFlowState.SUCCESS -> TripSuccessScreen(navController)
                else -> {
                    AddTripContent(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 16f.sx(s)),
                        uiState = uiState,

                        onTripNameChange = viewModel::onTripNameChange,
                        onCoverPhotoSelected = viewModel::onCoverPhotoSelected,

                        onDestinationSelected = viewModel::onDestinationSelected,
                        onThemeSelected = viewModel::onThemeSelected,
                        onDateClick = { showDatePicker = true },

                        onFromLocationChange = viewModel::onFromLocationChange,
                        onToLocationChange = viewModel::onToLocationChange,
                        onFromSuggestionSelected = viewModel::onFromSuggestionSelected,
                        onToSuggestionSelected = viewModel::onToSuggestionSelected,

                        onAddStop = viewModel::onAddStop,
                        onRemoveStop = viewModel::onRemoveStop,
                        onStopLocationChange = viewModel::onStopLocationChange,
                        onStopSuggestionSelected = viewModel::onStopSuggestionSelected,

                        onTransportSelectionChange = viewModel::onTransportSelectionChange,

                        onGenerateMapClick = viewModel::generateTripMap,
                        onCreateClick = viewModel::onCreateClick,

                        onFromClick = {
                            handle?.set(LOCATION_TARGET_KEY, LocationTarget.FROM.name)
                            handle?.remove<Int>(LOCATION_RESULT_KEY)
                            navController.navigate(Screen.LocationSearch.route)
                        },
                        onToClick = {
                            handle?.set(LOCATION_TARGET_KEY, LocationTarget.TO.name)
                            handle?.remove<Int>(LOCATION_RESULT_KEY)
                            navController.navigate(Screen.LocationSearch.route)
                        },
                        onStopClick = { index ->
                            handle?.set(LOCATION_TARGET_KEY, LocationTarget.STOP.name)
                            handle?.set(LOCATION_RESULT_KEY, index)
                            navController.navigate(Screen.LocationSearch.route)
                        }
                    )
                }
            }
        }

        if (showDatePicker) {
            AddTripDatePickerOverlay(
                initialStartDate = uiState.tripStartDate,
                initialEndDate = uiState.tripEndDate,
                onDismiss = { showDatePicker = false },
                onConfirm = {
                    viewModel.onDateSelected(it)
                    showDatePicker = false
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 412, heightDp = 1090)
@Composable
fun AddTripScreenPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            val navController = androidx.navigation.compose.rememberNavController()

            AddTripScreen(
                navController = navController
            )
        }
    }
}
