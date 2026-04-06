package com.example.memotrip_kroniq.ui.profile

import PreviewUiScaler
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.memotrip_kroniq.ui.components.PhotoPickerOverlay
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.profile.components.ProfileDateField
import com.example.memotrip_kroniq.ui.profile.components.ProfileHeaderSection
import com.example.memotrip_kroniq.ui.profile.components.ProfileInputField
import com.example.memotrip_kroniq.ui.profile.components.ProfileSectionLabel
import com.example.memotrip_kroniq.ui.profile.components.ProfileSegmentedSelector
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.utils.createImageFile
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.memotrip_kroniq.ui.profile.components.ProfileDatePickerOverlay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    navController: NavHostController? = null
) {
    val context = LocalContext.current

    val photoUri = remember { mutableStateOf<Uri?>(null) }
    val name = remember { mutableStateOf("") }
    val accountType = remember { mutableStateOf("Free") }
    val kroniqRole = remember { mutableStateOf("Admin") }
    val gender = remember { mutableStateOf("Female") }
    val firstName = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val dateOfBirth = remember { mutableStateOf("") }
    val selectedDateOfBirth = remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val showFirstNameError = remember { mutableStateOf(false) }
    val showLastNameError = remember { mutableStateOf(false) }
    val showDateOfBirthError = remember { mutableStateOf(false) }
    var showPhotoActionSheet by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri.value = uri
        }
        showPhotoActionSheet = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            photoUri.value = tempPhotoUri
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AppTopBar(
            title = "Profile",
            showBack = true,
            onBackClick = { navController?.popBackStack() },
            onMenuClick = null
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            ProfileHeaderSection(
                photoUri = photoUri.value,
                name = name.value,
                onPhotoClick = { showPhotoActionSheet = true },
                onNameChange = { name.value = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            ProfileSectionLabel(
                text = "Account type"
            )

            Spacer(modifier = Modifier.height(6.dp))

            ProfileSegmentedSelector(
                modifier = Modifier.fillMaxWidth(),
                options = listOf("Free", "Premium", "KroniQ"),
                selectedOption = accountType.value,
                onOptionSelected = { accountType.value = it },
                selectedColor = Color(0xFF86A96F)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSectionLabel(
                text = "KroniQ role",
                showLock = true
            )

            Spacer(modifier = Modifier.height(6.dp))

            ProfileSegmentedSelector(
                modifier = Modifier.fillMaxWidth(),
                options = listOf("Admin", "Member", "Host"),
                selectedOption = kroniqRole.value,
                onOptionSelected = { kroniqRole.value = it },
                selectedColor = Color(0xFF1686D9)
            )

            Spacer(modifier = Modifier.height(32.dp))

            ProfileSectionLabel(
                text = "Gender"
            )

            Spacer(modifier = Modifier.height(6.dp))

            ProfileSegmentedSelector(
                modifier = Modifier.fillMaxWidth(),
                options = listOf("Female", "Male"),
                selectedOption = gender.value,
                onOptionSelected = { gender.value = it },
                selectedColor = Color(0xFF1686D9)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSectionLabel(
                text = "First name"
            )

            Spacer(modifier = Modifier.height(6.dp))

            ProfileInputField(
                value = firstName.value,
                placeholder = "Add your name",
                onValueChange = {
                    firstName.value = it
                    if (it.isNotBlank()) showFirstNameError.value = false
                },
                error = showFirstNameError.value
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSectionLabel(
                text = "Last name"
            )

            Spacer(modifier = Modifier.height(6.dp))

            ProfileInputField(
                value = lastName.value,
                placeholder = "Add your last name",
                onValueChange = {
                    lastName.value = it
                    if (it.isNotBlank()) showLastNameError.value = false
                },
                error = showLastNameError.value
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSectionLabel(
                text = "E-mail"
            )

            Spacer(modifier = Modifier.height(6.dp))

            ProfileInputField(
                value = email.value,
                placeholder = "Type your e-mail",
                onValueChange = { email.value = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileSectionLabel(
                text = "Date of birth"
            )

            Spacer(modifier = Modifier.height(6.dp))

            ProfileDateField(
                value = dateOfBirth.value,
                onClick = {
                    showDatePicker = true
                },
                error = showDateOfBirthError.value
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                PrimaryButton(
                    text = "Next",
                    modifier = Modifier.padding(horizontal = 52.dp),
                    onClick = {
                        showFirstNameError.value = firstName.value.isBlank()
                        showLastNameError.value = lastName.value.isBlank()
                        showDateOfBirthError.value = dateOfBirth.value.isBlank()
                    }
                )
            }
        }
    }

    if (showPhotoActionSheet) {
        PhotoPickerOverlay(
            canDelete = photoUri.value != null,
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
                photoUri.value = null
                showPhotoActionSheet = false
            },
            onDismiss = { showPhotoActionSheet = false }
        )
    }

    if (showDatePicker) {
        ProfileDatePickerOverlay(
            initialDate = selectedDateOfBirth.value,
            onDismiss = {
                showDatePicker = false
            },
            onConfirm = { selectedDate ->
                selectedDateOfBirth.value = selectedDate
                dateOfBirth.value = selectedDate.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
                showDateOfBirthError.value = false
                showDatePicker = false
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun ProfileScreenPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            ProfileScreen(
                navController = rememberNavController()
            )
        }
    }
}