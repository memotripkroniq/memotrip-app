package com.example.memotrip_kroniq.ui.addtrip

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import PreviewUiScaler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.data.location.LocationSearchRepository
import com.example.memotrip_kroniq.data.network.HttpClientProvider
import com.example.memotrip_kroniq.data.tripmap.RemoteTripMapGenerator
import com.example.memotrip_kroniq.data.tripmap.TripMapGenerator

import com.example.memotrip_kroniq.ui.addtrip.components.AddTripDatePickerOverlay
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme


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

    val viewModel: AddTripViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {

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

    // 🔥 OVLÁDÁNÍ DATEPICKERU
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus() // 🔥 TADY SE ODEBERE FOCUS
            }
    ) {

        // ─────────────────────
        // 🧱 HLAVNÍ OBSAH
        // ─────────────────────
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

                onTransportSelectionChange = viewModel::onTransportSelectionChange,

                onGenerateMapClick = viewModel::generateTripMap, // ✅ HERO
                onCreateClick = viewModel::onCreateClick            // ✅ CREATE (validace)
            )

        }


        // ─────────────────────
        // 📅 DATE PICKER OVERLAY
        // ─────────────────────
        if (showDatePicker) {
            AddTripDatePickerOverlay(
                initialStartDate = uiState.tripStartDate,
                initialEndDate = uiState.tripEndDate,
                onDismiss = { showDatePicker = false },
                onConfirm = { range ->
                    viewModel.onDateSelected(range)
                    showDatePicker = false
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 412, heightDp = 1110)
@Composable
fun AddTripScreenPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            Scaffold(
                containerColor = Color.Black,
                topBar = {
                    AppTopBar(
                        title = "Add Trip",
                        showBack = true,
                        onBackClick = {}
                    )
                }
            ) { innerPadding ->
                AddTripContent(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 16f.sx(LocalUiScaler.current)),
                    uiState = AddTripUiState(
                        isThemesLocked = false,   // 🔓 ODEMČENO
                        selectedTheme = null,
                        destination = Destination.EUROPE,
                        transport = emptySet()
                        // transport = setOf(TransportType.CARAVAN)
                        // transport = setOf(TransportType.CAR, TransportType.CARAVAN)
                    ),
                    onTripNameChange = {},
                    onCoverPhotoSelected = {},
                    //generatedMapImageUrl = null,
                    onDestinationSelected = {},
                    onThemeSelected = {},
                    onDateClick = {},
                    onFromLocationChange = {},
                    onToLocationChange = {},
                    onFromSuggestionSelected = {},
                    onToSuggestionSelected = {},
                    onTransportSelectionChange = {},
                    onGenerateMapClick = {},
                    onCreateClick = {}
                )
            }
        }
    }
}



