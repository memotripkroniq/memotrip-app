package com.example.memotrip_kroniq.ui.addtrip


import PreviewUiScaler
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.data.location.LocationSuggestion
import com.example.memotrip_kroniq.ui.addtrip.components.AddStopButton
import com.example.memotrip_kroniq.ui.addtrip.components.AddTripHeroBanner
import com.example.memotrip_kroniq.ui.addtrip.components.AddTripNameField
import com.example.memotrip_kroniq.ui.addtrip.components.DateField
import com.example.memotrip_kroniq.ui.addtrip.components.DestinationSelector
import com.example.memotrip_kroniq.ui.addtrip.components.LocationField
import com.example.memotrip_kroniq.ui.components.PhotoPickerOverlay
import com.example.memotrip_kroniq.ui.addtrip.components.ThemeSelector
import com.example.memotrip_kroniq.ui.addtrip.components.TransportSelector
import com.example.memotrip_kroniq.ui.addtrip.components.WaypointField
import com.example.memotrip_kroniq.ui.addtrip.utils.AddTripScrollIndexMap
import com.example.memotrip_kroniq.ui.addtrip.utils.resolveFirstAddTripScrollTarget
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.model.Destination
import com.example.memotrip_kroniq.ui.core.model.ThemeType
import com.example.memotrip_kroniq.ui.core.model.TransportType
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.utils.createImageFile

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTripContent(
    modifier: Modifier = Modifier,
    uiState: AddTripUiState,
    submitButtonText: String = "",
    isSubmitEnabled: Boolean = !uiState.isGeneratingMap,
    showCoverPhotoPicker: Boolean = true,
    showThemeSelector: Boolean = true,
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
    onSubmitClick: () -> Unit,
    onGenerateMapClick: () -> Unit,
    onCoverPhotoSelected: (Uri?) -> Unit,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onStopClick: (index: Int) -> Unit,

    ) {
    val s = LocalUiScaler.current

    Log.d("COVER_UPLOAD", "AddTripContent composed")

    // 🧹 CLEANED – pouze state LazyColumn
    val listState = rememberLazyListState()

    var showPhotoActionSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        Log.d("COVER_UPLOAD", "AddTripContent gallery uri=$uri")
        if (uri != null) onCoverPhotoSelected(uri)
        showPhotoActionSheet = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        Log.d("COVER_UPLOAD", "AddTripContent camera success=$success temp=$tempPhotoUri")
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
                showPhotoPicker = showCoverPhotoPicker,
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
                error = uiState.showGeneratedMapError,
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

        if (showThemeSelector) {
            item {
                ThemeSelector(
                    selected = uiState.selectedTheme,
                    locked = uiState.isThemesLocked,
                    onSelect = onThemeSelected
                )
                Spacer(Modifier.height(20f.sy(s)))
            }
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

            Column {

                // FROM – klikneš jen na input
                LocationField(
                    label = stringResource(R.string.add_trip_from),
                    value = uiState.fromLocation.text,
                    error = uiState.showFromLocationError,
                    onClick = onFromClick,
                    bottomSpacing = 4.dp
                )

                // ADD STOP – NIC HO NEPŘEKRÝVÁ
                if (uiState.stops.size < 3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp)
                            .offset(y = (-1).dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AddStopButton(
                            visible = true,
                            onClick = onAddStop
                        )
                    }
                }

                Spacer(Modifier.height(0.dp))

                // WAYPOINTY
                uiState.stops.forEachIndexed { index, stop ->
                    WaypointField(
                        index = index,
                        value = stop.text,
                        error = uiState.showStopErrors.getOrNull(index) == true,
                        onClick = { onStopClick(index) },
                        onRemoveClick = { onRemoveStop(index) }
                    )

                    Spacer(Modifier.height(16.dp))
                }

                LocationField(
                    label = stringResource(R.string.add_trip_to),
                    value = uiState.toLocation.text,
                    error = uiState.showToLocationError,
                    onClick = onToClick,
                    modifier = Modifier.offset(y = (-10).dp)
                )
            }
        }


        item {
            Spacer(Modifier.height(6f.sy(s)))

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
                    text = if (uiState.isGeneratingMap) {
                        stringResource(R.string.add_trip_generating)
                    } else {
                        submitButtonText.ifBlank { stringResource(R.string.add_trip_create) }
                    },
                    enabled = isSubmitEnabled,
                    onClick = onSubmitClick,
                    modifier = Modifier.width(200f.sx(s))
                )
            }

            Spacer(Modifier.height(24f.sy(s)))
        }
    }

    // ✅ AUTO-SCROLL NA TRIP NAME PŘI CHYBĚ
    LaunchedEffect(
        uiState.showTripNameError,
        uiState.showGeneratedMapError,
        uiState.showDestinationError,
        uiState.showDateError,
        uiState.showFromLocationError,
        uiState.showStopErrors,
        uiState.showToLocationError,
        uiState.showTransportError
    ) {
        //Log.d("ADDTRIP_SCROLL", "showGeneratedMapError=${uiState.showGeneratedMapError}")
        val target = resolveFirstAddTripScrollTarget(uiState)
        //Log.d("ADDTRIP_SCROLL", "target=$target")
        val index = target?.let { AddTripScrollIndexMap[it] }
        if (index != null) listState.animateScrollToItem(index)
    }


    if (showCoverPhotoPicker && showPhotoActionSheet) {
        PhotoPickerOverlay(
            canDelete = uiState.coverPhotoUri != null,
            onTakePhoto = {
                if (
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
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
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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
                submitButtonText = "",
                isSubmitEnabled = true,
                showCoverPhotoPicker = true,
                showThemeSelector = true,
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
                onSubmitClick = {},
                onFromClick = {},
                onToClick = {},
                onStopClick = {}
            )
        }
    }
}
