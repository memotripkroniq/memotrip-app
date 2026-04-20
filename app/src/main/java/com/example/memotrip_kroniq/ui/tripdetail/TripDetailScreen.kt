package com.example.memotrip_kroniq.ui.tripdetail

import PreviewUiScaler
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.data.trips.TripsRepository
import com.example.memotrip_kroniq.navigation.Screen
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.tripdetail.components.BudgetEditField
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TripDetailScreen(
    navController: NavHostController,
    tripId: String
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Token store + Retrofit init
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

    val factory = remember(authRepository, tripsRepository, tripId) {
        TripDetailViewModelFactory(
            authRepository = authRepository,
            tripsRepository = tripsRepository,
            tripId = tripId
        )
    }

    val vm: TripDetailViewModel = viewModel(factory = factory)
    val uiState by vm.uiState.collectAsState()
    val canEditTrip = uiState.canEditTrip
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val savedStateHandle = currentBackStackEntry?.savedStateHandle
    val tripUpdated by remember(savedStateHandle) {
        savedStateHandle?.getStateFlow("trip_updated", false) ?: kotlinx.coroutines.flow.MutableStateFlow(false)
    }.collectAsState()

    LaunchedEffect(tripUpdated) {
        if (tripUpdated) {
            savedStateHandle?.set("trip_updated", false)
            vm.refreshTrip()
        }
    }

    val onBack = remember(vm, navController) {
        {
            if (vm.uiState.value.isSaving) return@remember

            if (!vm.uiState.value.canEditTrip) {
                navController.popBackStack()
            } else {
                vm.save {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("trip_updated", true)

                    navController.popBackStack()
                }
            }
        }
    }

    BackHandler {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
    ) {
        if (uiState.isInitialLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(color = Color.White)
            }
            return@Box
        }

        Scaffold(
            containerColor = Color.Black,
            topBar = {
                AppTopBar(
                    modifier = Modifier, // případně .statusBarsPadding()
                    title = uiState.tripName.ifBlank { stringResource(R.string.trip_detail_title_fallback) },
                    showBack = true,
                    centerTitle = true,
                    onBackClick = { onBack() },
                    onMenuClick = {
                        navController.navigate(Screen.Settings.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            TripDetailContent(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                uiState = uiState,
                onTabSelected = vm::onTabSelected,
                onToggleShareInKroniq = if (canEditTrip) vm::toggleShareInKroniq else ({}),
                onEditTripInfoClick = {
                    if (canEditTrip) {
                        navController.navigate(Screen.EditTrip.createRoute(tripId))
                    }
                },
                onAddMemberClick = if (canEditTrip) vm::onAddMemberClick else ({}),
                onAddChecklistItem = if (canEditTrip) vm::addChecklistItem else ({}),
                onToggleChecklistItem = if (canEditTrip) vm::toggleChecklistItem else ({ _ -> }),
                onRemoveChecklistItem = if (canEditTrip) vm::removeChecklistItem else ({ _ -> }),
                onDeleteTripClick = {
                    if (canEditTrip) {
                        vm.onDeleteTripClick {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("trip_deleted", true)

                            navController.popBackStack()
                        }
                    }
                },
                onStartEditChecklistItem = if (canEditTrip) vm::startEditChecklistItem else ({ _ -> }),
                onEditingChecklistTextChange = if (canEditTrip) vm::updateEditingChecklistText else ({}),
                onCommitEditChecklistItem = if (canEditTrip) vm::commitEditChecklistItem else ({}),
                onCancelEditChecklistItem = if (canEditTrip) vm::cancelEditChecklistItem else ({}),
                onAddNoteItem = if (canEditTrip) vm::addNoteItem else ({}),
                onRemoveNoteItem = if (canEditTrip) vm::removeNoteItem else ({ _ -> }),
                onStartEditNoteItem = if (canEditTrip) vm::startEditNoteItem else ({ _ -> }),
                onEditingNoteTextChange = if (canEditTrip) vm::updateEditingNoteText else ({}),
                onCommitEditNoteItem = if (canEditTrip) vm::commitEditNoteItem else ({}),
                onCancelEditNoteItem = if (canEditTrip) vm::cancelEditNoteItem else ({}),
                onToggleBudgetVisibility = vm::toggleBudgetVisibility,
                onStartEditBudget = if (canEditTrip) vm::startEditBudget else ({ _: BudgetEditField -> }),
                onEditingBudgetTextChange = if (canEditTrip) vm::updateEditingBudgetText else ({}),
                onCommitEditBudget = if (canEditTrip) vm::commitEditBudget else ({}),
                onTipsAddClick = if (canEditTrip) vm::addTipsAndTripsItem else ({}),
                onTipsCancelAddClick = if (canEditTrip) vm::cancelAddTipsAndTrips else ({}),
                onTipsRemoveItem = if (canEditTrip) vm::removeTipsAndTripsItem else ({ _ -> }),
                onStartEditTipsItem = if (canEditTrip) vm::startEditTipsAndTripsItem else ({ _ -> }),
                onEditingTipsTextChange = if (canEditTrip) vm::updateEditingTipsText else ({}),
                onCommitEditTipsItem = if (canEditTrip) vm::commitEditTipsAndTrips else ({}),
                onTipsRequestPickPhoto = if (canEditTrip) vm::requestPickTipsPhoto else ({ _ -> }),
                onTipsPhotoPicked = if (canEditTrip) vm::onTipsPhotoPicked else ({}),
                onCoverPhotoSelected = if (canEditTrip) vm::onCoverPhotoSelected else ({}),
                onAddPhotoCategory = if (canEditTrip) vm::createPhotoCategory else ({}),
                onRenamePhotoCategory = if (canEditTrip) vm::renamePhotoCategory else ({ _, _ -> }),
                onDeletePhotoCategory = if (canEditTrip) vm::deletePhotoCategory else ({}),
                onTripPhotoPicked = if (canEditTrip) vm::uploadTripPhoto else ({ _, _ -> }),
                onDeleteTripPhoto = if (canEditTrip) vm::deleteTripPhoto else ({}),
                onFromTextChange = if (canEditTrip) vm::onFromTextChange else ({}),
                onToTextChange = if (canEditTrip) vm::onToTextChange else ({}),
                onThemeSelected = if (canEditTrip) vm::onThemeSelected else ({}),
                onToggleTransport = if (canEditTrip) vm::toggleTransport else ({ _ -> }),


                )

            if (uiState.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 412, heightDp = 1290)
@Composable
fun TripDetailScreenPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            val navController = androidx.navigation.compose.rememberNavController()

            TripDetailScreen(
                navController = navController,
                tripId = "preview-trip-id"
            )
        }
    }
}
