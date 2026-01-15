package com.example.memotrip_kroniq.ui.addtrip

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.flow.MutableStateFlow

import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.location.LocationSearchRepository
import com.example.memotrip_kroniq.data.location.LocationSuggestion
import com.example.memotrip_kroniq.data.network.HttpClientProvider
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.data.tripmap.RemoteTripMapGenerator
import com.example.memotrip_kroniq.data.tripmap.TripMapGenerator
import com.example.memotrip_kroniq.navigation.*
import com.example.memotrip_kroniq.ui.addtrip.components.AddTripDatePickerOverlay
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.home.components.AppTopBar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTripScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val s = LocalUiScaler.current
    val focusManager = LocalFocusManager.current

    val tokenStore = remember { TokenDataStore(context) }

    val repository = remember {
        AuthRepository(
            api = RetrofitClient.api,
            tokenStore = tokenStore
        )
    }

    val viewModel: AddTripViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val tripMapGenerator: TripMapGenerator =
                    RemoteTripMapGenerator(HttpClientProvider.client)

                return AddTripViewModel(
                    authRepository = repository,
                    locationSearchRepository = LocationSearchRepository(HttpClientProvider.client),
                    tripMapGenerator = tripMapGenerator
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    // ✅ REAKTIVNĚ sledujeme entry a jeho savedStateHandle
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val handle = currentBackStackEntry?.savedStateHandle

    // --- výsledky z LocationSearch
    val locationName by remember(handle) {
        handle?.getStateFlow<String?>(LOCATION_NAME_KEY, null) ?: MutableStateFlow(null)
    }.collectAsState()

    val locationLat by remember(handle) {
        handle?.getStateFlow<Double?>(LOCATION_LAT_KEY, null) ?: MutableStateFlow(null)
    }.collectAsState()

    val locationLon by remember(handle) {
        handle?.getStateFlow<Double?>(LOCATION_LON_KEY, null) ?: MutableStateFlow(null)
    }.collectAsState()

    // --- a hlavně TARGET + STOP index (uložené před navigací!)
    val targetName by remember(handle) {
        handle?.getStateFlow<String?>(LOCATION_TARGET_KEY, null) ?: MutableStateFlow(null)
    }.collectAsState()

    // LOCATION_RESULT_KEY použijeme jako stopIndex (Int?)
    val stopIndex by remember(handle) {
        handle?.getStateFlow<Int?>(LOCATION_RESULT_KEY, null) ?: MutableStateFlow(null)
    }.collectAsState()

    // ✅ Jakmile dorazí komplet data, aplikujeme je do VM
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

        // 🧹 cleanup (aby se to nespouštělo znovu)
        handle?.apply {
            remove<String>(LOCATION_NAME_KEY)
            remove<Double>(LOCATION_LAT_KEY)
            remove<Double>(LOCATION_LON_KEY)
            remove<String>(LOCATION_TARGET_KEY)
            remove<Int>(LOCATION_RESULT_KEY)
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }

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
                    showBack = true,
                    onBackClick = { navController.popBackStack() }
                )
            }
        ) { innerPadding ->

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

                // ✅ ukládáme TARGET do savedStateHandle (ne do remember!)
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
                    handle?.set(LOCATION_RESULT_KEY, index) // stopIndex
                    navController.navigate(Screen.LocationSearch.route)
                }
            )
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
