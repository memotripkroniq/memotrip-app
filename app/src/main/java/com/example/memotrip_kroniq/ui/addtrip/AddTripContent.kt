package com.example.memotrip_kroniq.ui.addtrip

import PreviewUiScaler
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.memotrip_kroniq.data.location.LocationSuggestion
import com.example.memotrip_kroniq.navigation.LOCATION_TARGET_KEY
import com.example.memotrip_kroniq.navigation.LocationTarget
import com.example.memotrip_kroniq.navigation.Screen
import com.example.memotrip_kroniq.ui.addtrip.components.*
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.addtrip.utils.scrollToFirstAddTripError

import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTripContent(
    modifier: Modifier = Modifier,
    uiState: AddTripUiState,
    onTripNameChange: (String) -> Unit,
    onDestinationSelected: (Destination) -> Unit,
    onThemeSelected: (ThemeType) -> Unit,
    onDateClick: () -> Unit,
    onFromLocationChange: (TextFieldValue) -> Unit,
    onToLocationChange: (TextFieldValue) -> Unit,
    onFromSuggestionSelected: (LocationSuggestion) -> Unit,
    onToSuggestionSelected: (LocationSuggestion) -> Unit,
    onAddStop: () -> Unit,
    onRemoveStop: (index: Int) -> Unit,
    onStopLocationChange: (index: Int, value: TextFieldValue) -> Unit,
    onStopSuggestionSelected: (index: Int, suggestion: LocationSuggestion) -> Unit,
    onTransportSelectionChange: (Set<TransportType>) -> Unit,
    onCreateClick: () -> Unit,
    onGenerateMapClick: () -> Unit,
    onCoverPhotoSelected: (Uri?) -> Unit,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onStopClick: (index: Int) -> Unit,

    ) {
    val s = LocalUiScaler.current

    // 🧹 CLEANED – pouze state LazyColumn
    val listState = rememberLazyListState()

    var showPhotoActionSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onCoverPhotoSelected(uri)
        showPhotoActionSheet = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            onCoverPhotoSelected(tempPhotoUri)
        }
        showPhotoActionSheet = false
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                val photoFile = createImageFile(context)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }
        }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        state = listState
    ) {

        item {
            AddTripNameField(
                value = uiState.tripName,
                coverPhotoUri = uiState.coverPhotoUri,
                onValueChange = onTripNameChange,
                onAddPhotoClick = { showPhotoActionSheet = true },
                error = uiState.showTripNameError
            )
            Spacer(Modifier.height(16f.sy(s)))
        }

        item {
            AddTripHeroBanner(
                imageUrl = uiState.generatedMapImageUrl,
                isGenerating = uiState.isGeneratingMap,
                isMapDirty = uiState.isMapDirty,
                onGenerateClick = onGenerateMapClick
            )
            Spacer(Modifier.height(16f.sy(s)))
        }

        item {
            DestinationSelector(
                selected = uiState.destination,
                onSelect = onDestinationSelected,
                error = uiState.showDestinationError
            )
            Spacer(Modifier.height(20f.sy(s)))
        }

        item {
            ThemeSelector(
                selected = uiState.selectedTheme,
                locked = uiState.isThemesLocked,
                onSelect = onThemeSelected
            )
            Spacer(Modifier.height(20f.sy(s)))
        }

        item {
            DateField(
                startDate = uiState.tripStartDate,
                endDate = uiState.tripEndDate,
                error = uiState.showDateError,
                onClick = onDateClick
            )
            Spacer(Modifier.height(12f.sy(s)))
        }

        item {
            LocationField(
                label = "From",
                value = uiState.fromLocation.text,
                error = uiState.showFromLocationError,
                onClick = onFromClick
            )


            LocationSuggestionsDropdown(
                suggestions = uiState.fromSuggestions,
                onSelect = onFromSuggestionSelected
            )
        }

        item {
            AddStopButton(
                visible = uiState.stops.size < 3,
                onClick = onAddStop
            )
        }

        items(uiState.stops.size) { index ->
            val value = uiState.stops[index]

            Spacer(Modifier.height(12f.sy(s)))

            WaypointField(
                index = index,
                value = uiState.stops[index].text,
                error = uiState.showStopErrors.getOrNull(index) == true,
                onClick = { onStopClick(index) },
                onRemoveClick = { onRemoveStop(index) }
            )


            LocationSuggestionsDropdown(
                suggestions = uiState.stopSuggestions.getOrNull(index) ?: emptyList(),
                onSelect = { onStopSuggestionSelected(index, it) }
            )
        }

        item {
            Spacer(Modifier.height(12f.sy(s)))

            LocationField(
                label = "To",
                value = uiState.toLocation.text,
                error = uiState.showToLocationError,
                onClick = onToClick
            )

            LocationSuggestionsDropdown(
                suggestions = uiState.toSuggestions,
                onSelect = onToSuggestionSelected
            )
        }

        item {
            Spacer(Modifier.height(20f.sy(s)))

            TransportSelector(
                selected = uiState.transport,
                onSelectionChange = onTransportSelectionChange,
                error = uiState.showTransportError
            )

            Spacer(Modifier.height(28f.sy(s)))
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PrimaryButton(
                    text = if (uiState.isGeneratingMap) "Generating" else "Create",
                    enabled = !uiState.isGeneratingMap,
                    onClick = onCreateClick,
                    modifier = Modifier.width(200f.sx(s))
                )
            }

            Spacer(Modifier.height(24f.sy(s)))
        }
    }

    // ✅ AUTO-SCROLL NA TRIP NAME PŘI CHYBĚ
    LaunchedEffect(
        uiState.showTripNameError,
        uiState.showDestinationError,
        uiState.showDateError,
        uiState.showFromLocationError,
        uiState.showStopErrors,
        uiState.showToLocationError,
        uiState.showTransportError
    ) {
        scrollToFirstAddTripError(
            listState = listState,
            uiState = uiState
        )
    }

    if (showPhotoActionSheet) {
        AddTripPhotoOverlay(
            canDelete = uiState.coverPhotoUri != null,
            onTakePhoto = {
                if (
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val photoFile = createImageFile(context)
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    tempPhotoUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            },
            onPickFromGallery = { galleryLauncher.launch("image/*") },
            onDeletePhoto = {
                onCoverPhotoSelected(null)
                showPhotoActionSheet = false
            },
            onDismiss = { showPhotoActionSheet = false }
        )
    }
}

fun createImageFile(context: Context): File {
    val dir = File(context.cacheDir, "images")
    if (!dir.exists()) dir.mkdirs()
    return File(dir, "photo_${System.currentTimeMillis()}.jpg")
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 412, heightDp = 1090)
@Composable
fun AddTripContentPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            AddTripContent(
                modifier = Modifier,
                uiState = AddTripUiState(
                    tripName = "",
                    destination = Destination.EUROPE,
                    selectedTheme = ThemeType.SUMMER,
                    isThemesLocked = false,
                    tripStartDate = null,
                    tripEndDate = null,
                    fromLocation = TextFieldValue(""),
                    toLocation = TextFieldValue(""),
                    stops = emptyList(),
                    stopSuggestions = emptyList(),
                    transport = emptySet(),
                    generatedMapImageUrl = null,
                    isGeneratingMap = false
                ),
                onTripNameChange = {},
                onCoverPhotoSelected = {},
                onDestinationSelected = {},
                onThemeSelected = {},
                onDateClick = {},
                onFromLocationChange = {},
                onToLocationChange = {},
                onFromSuggestionSelected = {},
                onToSuggestionSelected = {},
                onAddStop = {},
                onRemoveStop = {},
                onStopLocationChange = { _, _ -> },
                onStopSuggestionSelected = { _, _ -> },
                onTransportSelectionChange = {},
                onGenerateMapClick = {},
                onCreateClick = {},
                onFromClick = {},
                onToClick = {},
                onStopClick = {}
            )
        }
    }
}
