package com.example.memotrip_kroniq.ui.tripdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.model.ThemeType
import com.example.memotrip_kroniq.ui.core.model.TransportType
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.tripdetail.components.*
import PreviewUiScaler
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.ui.addtrip.components.ThemeSelector
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.tripdetail.components.ZoomableImageDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.memotrip_kroniq.ui.components.PhotoPickerOverlay
import com.example.memotrip_kroniq.ui.utils.createImageFile
import androidx.compose.foundation.verticalScroll


@Composable
fun TripDetailContent(
    modifier: Modifier = Modifier,
    uiState: TripDetailUiState,
    onTabSelected: (TripDetailTab) -> Unit,
    onToggleShareInKroniq: () -> Unit,
    onEditTripInfoClick: () -> Unit,
    onAddMemberClick: () -> Unit,
    onDeleteTripClick: () -> Unit,
    onAddChecklistItem: () -> Unit,
    onToggleChecklistItem: (Int) -> Unit,
    onRemoveChecklistItem: (Int) -> Unit,
    onStartEditChecklistItem: (Int) -> Unit,
    onEditingChecklistTextChange: (TextFieldValue) -> Unit,
    onCommitEditChecklistItem: () -> Unit,
    onCancelEditChecklistItem: () -> Unit,
    onAddNoteItem: () -> Unit,
    onRemoveNoteItem: (Int) -> Unit,
    onStartEditNoteItem: (Int) -> Unit,
    onEditingNoteTextChange: (TextFieldValue) -> Unit,
    onCommitEditNoteItem: () -> Unit,
    onCancelEditNoteItem: () -> Unit,
    onToggleBudgetVisibility: () -> Unit,
    onStartEditBudget: (BudgetEditField) -> Unit,
    onEditingBudgetTextChange: (TextFieldValue) -> Unit,
    onCommitEditBudget: () -> Unit,
    onTipsAddClick: () -> Unit,
    onTipsCancelAddClick: () -> Unit,
    onTipsRemoveItem: (Int) -> Unit,

    onStartEditTipsItem: (Int) -> Unit,
    onEditingTipsTextChange: (TextFieldValue) -> Unit,
    onCommitEditTipsItem: () -> Unit,

    onTipsRequestPickPhoto: (Int) -> Unit,
    onTipsPhotoPicked: (Uri?) -> Unit,
    onCoverPhotoSelected: (Uri?) -> Unit,
    onAddPhotoCategory: (String) -> Unit,
    onRenamePhotoCategory: (String, String) -> Unit,
    onDeletePhotoCategory: (String) -> Unit,
    onTripPhotoPicked: (Uri, String?) -> Unit,
    onDeleteTripPhoto: (String) -> Unit,
    onFromTextChange: (String) -> Unit,
    onToTextChange: (String) -> Unit,
    onThemeSelected: (ThemeType?) -> Unit,
    onToggleTransport: (TransportType) -> Unit,


    ) {
    val s = LocalUiScaler.current

    var showPhotoActionSheet by remember { mutableStateOf(false) }
    var showTripPhotoActionSheet by remember { mutableStateOf(false) }
    var selectedTripPhotoCategoryId by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempTripPhotoUri by remember { mutableStateOf<Uri?>(null) }

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

    val tripPhotoGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onTripPhotoPicked(uri, selectedTripPhotoCategoryId)
        showTripPhotoActionSheet = false
    }

    val tripPhotoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempTripPhotoUri != null) {
            onTripPhotoPicked(tempTripPhotoUri!!, selectedTripPhotoCategoryId)
        }
        showTripPhotoActionSheet = false
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                val photoFile = createImageFile(context)
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }
        }

    if (uiState.isInitialLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    var showMap by remember { mutableStateOf(false) }
    val fullUrl = uiState.mapFullImageUrl

    if (uiState.selectedTab == TripDetailTab.PHOTOS) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                TripHeroSection(
                    coverUrl = uiState.localCoverPhotoUri?.toString() ?: uiState.coverImageUrl,
                    mapUrl = uiState.mapImageUrl,
                    canEdit = uiState.canEditTrip,
                    onChangeCoverClick = { showPhotoActionSheet = true },
                    onMapClick = {
                        if (!uiState.mapImageUrl.isNullOrBlank()) showMap = true
                    }
                )

                Spacer(Modifier.height(16f.sy(s)))

                TripTabs(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = onTabSelected
                )

                Spacer(Modifier.height(20f.sy(s)))
            }

                TripPhotosTab(
                    modifier = Modifier.weight(1f),
                    categories = uiState.photoCategories,
                    photos = uiState.tripPhotos,
                    isLoading = uiState.isPhotosLoading,
                    canEdit = uiState.canEditTrip,
                    isAddPhotoEnabled = uiState.canAddTripPhoto,
                    onAddCategoryClick = onAddPhotoCategory,
                    onRenameCategoryClick = onRenamePhotoCategory,
                    onDeleteCategoryClick = onDeletePhotoCategory,
                onAddPhotoClick = { categoryId ->
                    selectedTripPhotoCategoryId = categoryId
                    showTripPhotoActionSheet = true
                },
                onDeletePhotoClick = onDeleteTripPhoto
            )
        }

        if (showMap && !fullUrl.isNullOrBlank()) {
            ZoomableImageDialog(
                imageUrl = fullUrl,
                onDismiss = { showMap = false }
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            item {
                TripHeroSection(
                    coverUrl = uiState.localCoverPhotoUri?.toString() ?: uiState.coverImageUrl,
                    mapUrl = uiState.mapImageUrl,
                    canEdit = uiState.canEditTrip,
                    onChangeCoverClick = { showPhotoActionSheet = true },
                    onMapClick = {
                        if (!uiState.mapImageUrl.isNullOrBlank()) showMap = true
                    }
                )

                Spacer(Modifier.height(16f.sy(s)))
            }

            item {
                TripTabs(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = onTabSelected
                )
                Spacer(Modifier.height(20f.sy(s)))
            }

            when (uiState.selectedTab) {
            TripDetailTab.DETAILS -> {
                item {
                    TripMembersSection(
                        members = uiState.members,
                        onAddMemberClick = onAddMemberClick
                    )
                    Spacer(Modifier.height(20f.sy(s)))
                }

                item {
                    ShareInKroniqSection(
                        checked = uiState.isSharedInKroniq,
                        locked = uiState.isKroniqLocked || !uiState.canEditTrip,
                        isUpdating = uiState.isShareInKroniqUpdating,
                        errorMessage = uiState.shareInKroniqErrorMessage,
                        onToggle = onToggleShareInKroniq
                    )
                    Spacer(Modifier.height(20f.sy(s)))
                }

                item {
                    ThemeSelector(
                        selected = uiState.selectedTheme,
                        locked = uiState.isThemesLocked || !uiState.canEditTrip,
                        onSelect = { theme ->
                            onThemeSelected(theme)
                        }
                    )
                    Spacer(Modifier.height(20f.sy(s)))
                }


                item {
                    TripInfoCard(
                        dateText = uiState.tripDateText,
                        fromText = uiState.fromText,
                        toText = uiState.toText,
                        transport = uiState.transport,
                        canEdit = uiState.canEditTrip,
                        onEditClick = onEditTripInfoClick,
                        theme = uiState.selectedTheme
                    )
                    Spacer(Modifier.height(20f.sy(s)))
                }

                item {
                    ChecklistCard(
                        items = uiState.checklistItems,
                        editingIndex = uiState.editingChecklistIndex,
                        editingText = uiState.editingChecklistText,
                        onAddClick = onAddChecklistItem,
                        onToggleChecked = onToggleChecklistItem,
                        onRemoveItem = onRemoveChecklistItem,
                        onStartEdit = onStartEditChecklistItem,
                        onEditingTextChange = onEditingChecklistTextChange,
                        onCommitEdit = onCommitEditChecklistItem,
                        onCancelEdit = onCancelEditChecklistItem
                    )

                    Spacer(Modifier.height(16f.sy(s)))
                }

                item {
                    NotesCard(
                        items = uiState.notes,
                        onAddClick = onAddNoteItem,
                        onRemoveItem = { index -> onRemoveNoteItem(index) },
                        editingIndex = uiState.editingNoteIndex,
                        editingText = uiState.editingNoteText,
                        onStartEdit = { index -> onStartEditNoteItem(index) },
                        onEditingTextChange = onEditingNoteTextChange,
                        onCommitEdit = onCommitEditNoteItem,
                        onCancelEdit = onCancelEditNoteItem
                    )
                    Spacer(Modifier.height(16f.sy(s)))
                }

                item {
                    BudgetCard(
                        plannedAmount = uiState.plannedBudget,
                        spentAmount = uiState.spentBudget,
                        isVisible = uiState.isBudgetVisible,
                        onToggleVisibility = onToggleBudgetVisibility,
                        editingField = uiState.editingBudgetField,
                        editingText = uiState.editingBudgetText,
                        onStartEdit = onStartEditBudget,
                        onEditingTextChange = onEditingBudgetTextChange,
                        onCommitEdit = onCommitEditBudget
                    )

                    Spacer(Modifier.height(16f.sy(s)))
                }

                item {
                    val tipsImagePicker = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        onTipsPhotoPicked(uri)
                    }

                    TipsAndTripsCard(
                        items = uiState.tipsAndTripsItems,
                        isAdding = uiState.isTipsAndTripsAdding,
                        editingIndex = uiState.editingTipsIndex,
                        editingText = uiState.editingTipsText,
                        onStartEdit = onStartEditTipsItem,
                        onEditingTextChange = onEditingTipsTextChange,
                        onCommitEdit = onCommitEditTipsItem,
                        onAddClick = onTipsAddClick,
                        onCancelAddClick = onTipsCancelAddClick,
                        onPickImageClick = {
                            val idx = uiState.editingTipsIndex
                                ?: uiState.tipsAndTripsItems.lastIndex.takeIf { it >= 0 }
                                ?: return@TipsAndTripsCard

                            onTipsRequestPickPhoto(idx)
                            tipsImagePicker.launch("image/*")
                        },
                        onAddItemPhotoClick = { index ->
                            onTipsRequestPickPhoto(index)
                            tipsImagePicker.launch("image/*")
                        },
                        onRemoveItem = onTipsRemoveItem
                    )

                    Spacer(Modifier.height(24f.sy(s)))
                }

                if (uiState.canEditTrip) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PrimaryButton(
                                text = stringResource(R.string.trip_detail_delete_trip),
                                onClick = onDeleteTripClick,
                                modifier = Modifier
                                    .width(200f.sx(s))
                                    .align(Alignment.Center)
                            )
                        }

                        Spacer(Modifier.height(24f.sy(s)))
                    }
                }
            }

            TripDetailTab.PHOTOS -> {
                // handled in dedicated branch above
            }

            TripDetailTab.EXPORTS -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                            .background(Color(0xFF383A41))
                            .padding(horizontal = 20.dp, vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.trip_detail_exports_placeholder),
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }

                    Spacer(Modifier.height(24f.sy(s)))
                }
            }
        }
        }
        if (showMap && !fullUrl.isNullOrBlank()) {
            ZoomableImageDialog(
                imageUrl = fullUrl,
                onDismiss = { showMap = false }
            )
        }
    }

    if (showPhotoActionSheet && uiState.canEditTrip) {
        PhotoPickerOverlay(
            canDelete = uiState.coverImageUrl != null,
            onTakePhoto = {
                showPhotoActionSheet = false
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
            onPickFromGallery = {
                showPhotoActionSheet = false
                galleryLauncher.launch("image/*")
            },
            onDeletePhoto = {
                onCoverPhotoSelected(null)
                showPhotoActionSheet = false
            },
            onDismiss = { showPhotoActionSheet = false }
        )
    }

    if (showTripPhotoActionSheet && uiState.canEditTrip) {
        PhotoPickerOverlay(
            canDelete = false,
            showDeleteAction = false,
            onTakePhoto = {
                showTripPhotoActionSheet = false
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
                    tempTripPhotoUri = uri
                    tripPhotoCameraLauncher.launch(uri)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onPickFromGallery = {
                showTripPhotoActionSheet = false
                tripPhotoGalleryLauncher.launch("image/*")
            },
            onDeletePhoto = {},
            onDismiss = { showTripPhotoActionSheet = false }
        )
    }
}

@Preview(
    name = "TripDetailContent",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 1800
)
@Composable
private fun TripDetailContentPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            TripDetailContent(
                uiState = TripDetailUiState(
                    isInitialLoading = false,
                    members = listOf(
                        TripMemberUi("1", "Kristin", avatarRes = R.drawable.some_avatar_kristin),
                        TripMemberUi("2", "Peetr", avatarRes = R.drawable.some_avatar_peetr),
                        TripMemberUi("3", "Lily", avatarRes = R.drawable.some_avatar_lily),
                        TripMemberUi("4", "Sara", avatarRes = R.drawable.some_avatar_sara)
                    ),
                    themes = listOf(
                        ThemeUi(ThemeType.SUMMER, R.drawable.homescreen_theme_summer),
                        ThemeUi(ThemeType.WINTER, R.drawable.homescreen_theme_winter),
                        ThemeUi(ThemeType.CAMPING, R.drawable.homescreen_theme_camping),
                        ThemeUi(ThemeType.CITIES, R.drawable.homescreen_theme_cities),
                        ThemeUi(ThemeType.NATURE, R.drawable.homescreen_theme_nature)
                    ),
                    selectedTheme = ThemeType.SUMMER,
                    isThemesLocked = false,
                    tripDateText = "28 June 2025 - 11 July 2025",
                    fromText = "Slovenský Grob, Slovakia",
                    toText = "Camping Lacona Pineta, Elba",
                    transport = setOf(TransportType.PLANE, TransportType.CAR),
                    checklistItems = listOf(
                        ChecklistItemUi("Passport", checked = true),
                        ChecklistItemUi("Powerbank", checked = false)
                    ),
                    notes = listOf(
                        NoteItemUi("Buy groceries before trip"),
                        NoteItemUi("Check ferry times")
                    ),
                    plannedBudget = "1 200 €",
                    spentBudget = "340 €",
                    isBudgetVisible = true,
                    tipsAndTripsItems = listOf(
                        TipsAndTripsItemUi(title = "Hidden beach"),
                        TipsAndTripsItemUi(title = "Try local market")
                    ),
                    hasKroniqPackage = false,
                    isSharedInKroniq = false
                ),
                onTabSelected = {},
                onToggleShareInKroniq = {},
                onEditTripInfoClick = {},
                onAddMemberClick = {},
                onDeleteTripClick = {},
                onAddChecklistItem = {},
                onToggleChecklistItem = {},
                onRemoveChecklistItem = {},
                onStartEditChecklistItem = {},
                onEditingChecklistTextChange = {},
                onCommitEditChecklistItem = {},
                onCancelEditChecklistItem = {},
                onAddNoteItem = {},
                onRemoveNoteItem = {},
                onStartEditNoteItem = {},
                onEditingNoteTextChange = {},
                onCommitEditNoteItem = {},
                onCancelEditNoteItem = {},
                onToggleBudgetVisibility = {},
                onStartEditBudget = { _: BudgetEditField -> },
                onEditingBudgetTextChange = { _: TextFieldValue -> },
                onCommitEditBudget = {},
                onTipsAddClick = {},
                onTipsCancelAddClick = {},
                onTipsRemoveItem = {},
                onStartEditTipsItem = {},
                onEditingTipsTextChange = {},
                onCommitEditTipsItem = {},
                onTipsRequestPickPhoto = {},
                onTipsPhotoPicked = {},
                onCoverPhotoSelected = {},
                onAddPhotoCategory = {},
                onRenamePhotoCategory = { _, _ -> },
                onDeletePhotoCategory = {},
                onTripPhotoPicked = { _, _ -> },
                onDeleteTripPhoto = {},
                onFromTextChange = {},
                onToTextChange = {},
                onThemeSelected = {},
                onToggleTransport = {}
            )
        }
    }
}

