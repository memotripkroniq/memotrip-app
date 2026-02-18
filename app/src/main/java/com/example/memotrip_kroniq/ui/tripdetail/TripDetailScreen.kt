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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.data.trips.TripsRepository
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

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

    val factory = remember(tripsRepository, tripId) {
        TripDetailViewModelFactory(
            tripsRepository = tripsRepository,
            tripId = tripId
        )
    }

    val vm: TripDetailViewModel = viewModel(factory = factory)
    val uiState by vm.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
    ) {
        Scaffold(
            containerColor = Color.Black,
            topBar = {
                AppTopBar(
                    modifier = Modifier, // případně .statusBarsPadding()
                    title = "Trip detail",
                    showBack = true,
                    onBackClick = { navController.popBackStack() }
                )
            }
        ) { innerPadding ->
            TripDetailContent(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                uiState = uiState,
                onTabSelected = vm::onTabSelected,
                onAddMemberClick = vm::onAddMemberClick,
                onAddChecklistItem = vm::addChecklistItem,
                onToggleChecklistItem = vm::toggleChecklistItem,
                onRemoveChecklistItem = vm::removeChecklistItem,
                onDeleteTripClick = vm::onDeleteTripClick,
                onStartEditChecklistItem = vm::startEditChecklistItem,
                onEditingChecklistTextChange = vm::updateEditingChecklistText,
                onCommitEditChecklistItem = vm::commitEditChecklistItem,
                onCancelEditChecklistItem = vm::cancelEditChecklistItem,
                onAddNoteItem = vm::addNoteItem,
                onRemoveNoteItem = vm::removeNoteItem,
                onStartEditNoteItem = vm::startEditNoteItem,
                onEditingNoteTextChange = vm::updateEditingNoteText,
                onCommitEditNoteItem = vm::commitEditNoteItem,
                onCancelEditNoteItem = vm::cancelEditNoteItem,
                onToggleBudgetVisibility = vm::toggleBudgetVisibility,
                onStartEditBudget = vm::startEditBudget,
                onEditingBudgetTextChange = vm::updateEditingBudgetText,
                onCommitEditBudget = vm::commitEditBudget,

                )
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
