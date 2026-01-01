package com.example.memotrip_kroniq.ui.addtrip

import PreviewUiScaler
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.memotrip_kroniq.ui.addtrip.components.AddTripHeroBanner
import com.example.memotrip_kroniq.ui.addtrip.components.AddTripNameField
import com.example.memotrip_kroniq.ui.addtrip.components.DateField
import com.example.memotrip_kroniq.ui.addtrip.components.DestinationSelector
import com.example.memotrip_kroniq.ui.addtrip.components.LocationField
import com.example.memotrip_kroniq.ui.addtrip.components.ThemeSelector
import com.example.memotrip_kroniq.ui.addtrip.components.TransportSelector
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.memotrip_kroniq.ui.addtrip.components.AddTripPhotoOverlay
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.memotrip_kroniq.data.location.LocationSuggestion
import com.example.memotrip_kroniq.ui.addtrip.components.LocationSuggestionsDropdown
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
    onFromLocationChange: (String) -> Unit,
    onToLocationChange: (String) -> Unit,
    onFromSuggestionSelected: (LocationSuggestion) -> Unit,
    onToSuggestionSelected: (LocationSuggestion) -> Unit,
    onTransportSelectionChange: (Set<TransportType>) -> Unit,
    onCreateClick: () -> Unit,
    onGenerateMapClick: () -> Unit,
    onCoverPhotoSelected: (Uri?) -> Unit
) {
    val s = LocalUiScaler.current
    var showPhotoActionSheet by remember { mutableStateOf(false) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onCoverPhotoSelected(uri)
        }
        showPhotoActionSheet = false
    }
    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
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

    val destinationError = uiState.destination == null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
    ) {

        /* ✍️ Trip name */
        AddTripNameField(
            value = uiState.tripName,
            coverPhotoUri = uiState.coverPhotoUri,
            onValueChange = onTripNameChange,
            onAddPhotoClick = { showPhotoActionSheet = true },
            error = uiState.showTripNameError
        )

        Spacer(modifier = Modifier.height(16f.sy(s)))

        /* 🌍 Hero banner */
        AddTripHeroBanner(
            imageUrl = uiState.generatedMapImageUrl,
            isGenerating = uiState.isGeneratingMap,
            onGenerateClick = onGenerateMapClick
            //modifier = Modifier.padding(horizontal = 16.dp)
        )


        Spacer(modifier = Modifier.height(16f.sy(s)))

        /* 🌍 Destination */
        DestinationSelector(
            selected = uiState.destination,
            onSelect = onDestinationSelected,
            error = uiState.showDestinationError
        )

        Spacer(modifier = Modifier.height(20f.sy(s)))

        /* 🎨 Theme */
        ThemeSelector(
            selected = uiState.selectedTheme,
            locked = uiState.isThemesLocked,
            onSelect = onThemeSelected
        )


        Spacer(modifier = Modifier.height(20f.sy(s)))

        /* 📅 Date */
        DateField(
            startDate = uiState.tripStartDate,
            endDate = uiState.tripEndDate,
            error = uiState.showDateError,
            onClick = onDateClick
        )

        Spacer(modifier = Modifier.height(12f.sy(s)))

        /* 📍 From / To */
        Column {

            LocationField(
                label = "From",
                value = uiState.fromLocation,
                onValueChange = onFromLocationChange,
                error = uiState.showFromLocationError
            )

            // ⬇️ Dropdown hned POD inputem
            LocationSuggestionsDropdown(
                suggestions = uiState.fromSuggestions,
                onSelect = onFromSuggestionSelected
            )
        }

        Spacer(modifier = Modifier.height(12f.sy(s)))

        Column {

            LocationField(
                label = "To",
                value = uiState.toLocation,
                onValueChange = onToLocationChange,
                error = uiState.showToLocationError
            )

            LocationSuggestionsDropdown(
                suggestions = uiState.toSuggestions,
                onSelect = onToSuggestionSelected
            )
        }

        Spacer(modifier = Modifier.height(20f.sy(s)))

        /* 🚗 Transport */
        TransportSelector(
            selected = uiState.transport,
            onSelectionChange = onTransportSelectionChange,
            error = uiState.showTransportError
        )

        Spacer(modifier = Modifier.height(28f.sy(s)))

        /* ▶️ Create */
        PrimaryButton(
            text = if (uiState.isGeneratingMap) "Generating..." else "Create",
            enabled = !uiState.isGeneratingMap,
            onClick = onCreateClick,
            modifier = Modifier
                .width(200f.sx(s))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24f.sy(s)))
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
            onPickFromGallery = {
                galleryLauncher.launch("image/*")   // 👈 TADY
            },
            onDeletePhoto = {
                onCoverPhotoSelected(null)
                showPhotoActionSheet = false
            },
            onDismiss = {
                showPhotoActionSheet = false
            }
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
                    //tripStartDate = LocalDate.of(2025, 6, 28),
                    //tripEndDate = LocalDate.of(2025, 7, 11),
                    fromLocation = "",
                    toLocation = "",
                    transport = emptySet(),
                    // transport = setOf(TransportType.CARAVAN)
                    // transport = setOf(TransportType.CAR, TransportType.CARAVAN)
                    generatedMapImageUrl = null,   // ⬅️ default mapa (Generate overlay)
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
                onTransportSelectionChange = {},
                onGenerateMapClick = {},
                onCreateClick = {}
            )
        }
    }
}

