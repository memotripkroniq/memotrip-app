package com.example.memotrip_kroniq.ui.edittrip

import PreviewUiScaler
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
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
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.navigation.LOCATION_LAT_KEY
import com.example.memotrip_kroniq.navigation.LOCATION_LON_KEY
import com.example.memotrip_kroniq.navigation.LOCATION_NAME_KEY
import com.example.memotrip_kroniq.navigation.LOCATION_RESULT_KEY
import com.example.memotrip_kroniq.navigation.LOCATION_TARGET_KEY
import com.example.memotrip_kroniq.navigation.LocationTarget
import com.example.memotrip_kroniq.navigation.Screen
import com.example.memotrip_kroniq.ui.addtrip.AddTripContent
import com.example.memotrip_kroniq.ui.addtrip.components.AddTripDatePickerOverlay
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import kotlinx.coroutines.flow.MutableStateFlow

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditTripScreen(
    navController: NavHostController,
    tripId: String
) {
    val context = LocalContext.current
    val s = LocalUiScaler.current
    val focusManager = LocalFocusManager.current
    var showDatePicker by remember { mutableStateOf(false) }

    val tokenStore = remember(context) { TokenDataStore(context) }
    remember(tokenStore) {
        RetrofitClient.build(tokenStore)
        true
    }

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
            client = HttpClientProvider.client,
            tokenStore = tokenStore
        )
    }

    val factory = remember(tripsRepository, authRepository, tripMapGenerator, tripId) {
        EditTripViewModelFactory(
            tripsRepository = tripsRepository,
            authRepository = authRepository,
            tripMapGenerator = tripMapGenerator,
            tripId = tripId
        )
    }

    val viewModel: EditTripViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

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
                    title = stringResource(R.string.edit_trip_title),
                    showBack = true,
                    onBackClick = { navController.popBackStack() }
                )
            }
        ) { innerPadding ->
            if (uiState.isInitialLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                AddTripContent(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 16f.sx(s)),
                    uiState = uiState.formState,
                    submitButtonText = if (uiState.isSaving) {
                        stringResource(R.string.edit_trip_saving)
                    } else {
                        stringResource(R.string.edit_trip_save)
                    },
                    isSubmitEnabled = !uiState.isSaving && !uiState.formState.isGeneratingMap,
                    showCoverPhotoPicker = false,
                    showThemeSelector = false,
                    onTripNameChange = viewModel::onTripNameChange,
                    onCoverPhotoSelected = viewModel::onCoverPhotoSelected,
                    onDestinationSelected = viewModel::onDestinationSelected,
                    onThemeSelected = viewModel::onThemeSelected,
                    onDateClick = { showDatePicker = true },
                    onFromLocationChange = {},
                    onToLocationChange = {},
                    onFromSuggestionSelected = viewModel::onFromSuggestionSelected,
                    onToSuggestionSelected = viewModel::onToSuggestionSelected,
                    onAddStop = viewModel::onAddStop,
                    onRemoveStop = viewModel::onRemoveStop,
                    onStopLocationChange = { _, _ -> },
                    onStopSuggestionSelected = viewModel::onStopSuggestionSelected,
                    onTransportSelectionChange = viewModel::onTransportSelectionChange,
                    onGenerateMapClick = viewModel::generateTripMap,
                    onSubmitClick = {
                        viewModel.onSaveClick {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("trip_updated", true)
                            navController.popBackStack()
                        }
                    },
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

        if (showDatePicker) {
            AddTripDatePickerOverlay(
                initialStartDate = uiState.formState.tripStartDate,
                initialEndDate = uiState.formState.tripEndDate,
                onDismiss = { showDatePicker = false },
                onConfirm = {
                    viewModel.onDateSelected(it)
                    showDatePicker = false
                }
            )
        }

        if (uiState.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 412, heightDp = 1090)
@Composable
fun EditTripScreenPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            val navController = androidx.navigation.compose.rememberNavController()
            EditTripScreen(
                navController = navController,
                tripId = "preview-trip-id"
            )
        }
    }
}
