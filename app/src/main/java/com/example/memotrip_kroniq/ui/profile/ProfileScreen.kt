package com.example.memotrip_kroniq.ui.profile

import PreviewUiScaler
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
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
import com.example.memotrip_kroniq.ui.profile.components.ProfileSegmentSelector
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.utils.createImageFile
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.profile.components.ProfileDatePickerOverlay
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    navController: NavHostController? = null,
    initialName: String = "",
    initialAccountType: String = "Free",
    initialEmail: String = "",
    initialGender: String = "",
    initialFirstName: String = "",
    initialLastName: String = "",
    initialDateOfBirth: String = "",
    initialProfileImageUrl: String = "",
    onSaveClick: (
        photoUri: Uri?,
        isPhotoRemoved: Boolean,
        name: String,
        accountType: String,
        kroniqRole: String,
        gender: String,
        firstName: String,
        lastName: String,
        email: String,
        dateOfBirth: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val photoUri = remember { mutableStateOf<Uri?>(null) }
    val profileImageUrl = remember(initialProfileImageUrl) { mutableStateOf(initialProfileImageUrl) }
    val name = remember(initialName) { mutableStateOf(initialName) }
    val accountType = remember(initialAccountType) { mutableStateOf(initialAccountType) }
    val kroniqRole = remember { mutableStateOf("Admin") }
    val gender = remember(initialGender) { mutableStateOf(initialGender) }
    val firstName = remember(initialFirstName) { mutableStateOf(initialFirstName) }
    val lastName = remember(initialLastName) { mutableStateOf(initialLastName) }
    val email = remember(initialEmail) { mutableStateOf(initialEmail) }
    val parsedInitialDateOfBirth = remember(initialDateOfBirth) {
        parseInitialDateOfBirth(initialDateOfBirth)
    }
    val dateOfBirth = remember(initialDateOfBirth) {
        mutableStateOf(
            parsedInitialDateOfBirth?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).orEmpty()
        )
    }
    val selectedDateOfBirth = remember(initialDateOfBirth) {
        mutableStateOf(parsedInitialDateOfBirth)
    }
    var showDatePicker by remember { mutableStateOf(false) }
    val showGenderError = remember { mutableStateOf(false) }
    val showFirstNameError = remember { mutableStateOf(false) }
    val showLastNameError = remember { mutableStateOf(false) }
    val showDateOfBirthError = remember { mutableStateOf(false) }
    var showPhotoActionSheet by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isPhotoRemoved by remember(initialProfileImageUrl) { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri.value = uri
            profileImageUrl.value = ""
            isPhotoRemoved = false
        }
        showPhotoActionSheet = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            photoUri.value = tempPhotoUri
            profileImageUrl.value = ""
            isPhotoRemoved = false
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppTopBar(
                title = stringResource(R.string.profile_title),
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
                    photoModel = photoUri.value ?: profileImageUrl.value.takeIf { it.isNotBlank() },
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
                    text = stringResource(R.string.profile_account_type)
                )

                Spacer(modifier = Modifier.height(6.dp))

                ProfileSegmentSelector(
                    modifier = Modifier.fillMaxWidth(),
                    options = listOf("Free", "Premium", "KroniQ"),
                    selectedOption = accountType.value,
                    optionLabels = mapOf(
                        "Free" to stringResource(R.string.profile_account_type_free),
                        "Premium" to stringResource(R.string.profile_account_type_premium),
                        "KroniQ" to stringResource(R.string.profile_account_type_kroniq)
                    ),
                    onOptionSelected = {},
                    selectedColor = Color(0xFF86A96F),
                    enabled = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileSectionLabel(
                    text = stringResource(R.string.profile_kroniq_role),
                    showLock = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                ProfileSegmentSelector(
                    modifier = Modifier.fillMaxWidth(),
                    options = listOf("Admin", "Member", "Host"),
                    selectedOption = kroniqRole.value,
                    optionLabels = mapOf(
                        "Admin" to stringResource(R.string.profile_role_admin),
                        "Member" to stringResource(R.string.profile_role_member),
                        "Host" to stringResource(R.string.profile_role_host)
                    ),
                    onOptionSelected = { kroniqRole.value = it },
                    selectedColor = Color(0xFF1686D9)
                )

                Spacer(modifier = Modifier.height(32.dp))

                ProfileSectionLabel(
                    text = stringResource(R.string.profile_gender)
                )

                Spacer(modifier = Modifier.height(6.dp))

                ProfileSegmentSelector(
                    modifier = Modifier.fillMaxWidth(),
                    options = listOf("Female", "Male"),
                    selectedOption = gender.value,
                    optionLabels = mapOf(
                        "Female" to stringResource(R.string.profile_gender_female),
                        "Male" to stringResource(R.string.profile_gender_male)
                    ),
                    onOptionSelected = {
                        gender.value = it
                        showGenderError.value = false
                    },
                    selectedColor = Color(0xFF1686D9),
                    error = showGenderError.value
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileSectionLabel(
                    text = stringResource(R.string.profile_first_name)
                )

                Spacer(modifier = Modifier.height(6.dp))

                ProfileInputField(
                    value = firstName.value,
                    placeholder = stringResource(R.string.profile_first_name_placeholder),
                    onValueChange = {
                        firstName.value = it
                        if (it.isNotBlank()) showFirstNameError.value = false
                    },
                    error = showFirstNameError.value
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileSectionLabel(
                    text = stringResource(R.string.profile_last_name)
                )

                Spacer(modifier = Modifier.height(6.dp))

                ProfileInputField(
                    value = lastName.value,
                    placeholder = stringResource(R.string.profile_last_name_placeholder),
                    onValueChange = {
                        lastName.value = it
                        if (it.isNotBlank()) showLastNameError.value = false
                    },
                    error = showLastNameError.value
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileSectionLabel(
                    text = stringResource(R.string.profile_email)
                )

                Spacer(modifier = Modifier.height(6.dp))

                ProfileInputField(
                    value = email.value,
                    placeholder = stringResource(R.string.profile_email_placeholder),
                    onValueChange = { email.value = it },
                    enabled = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileSectionLabel(
                    text = stringResource(R.string.profile_date_of_birth)
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
                        text = stringResource(R.string.profile_save),
                        modifier = Modifier.padding(horizontal = 52.dp),
                        onClick = {
                            val genderEmpty = gender.value.isBlank()
                            val firstNameEmpty = firstName.value.isBlank()
                            val lastNameEmpty = lastName.value.isBlank()
                            val dateOfBirthEmpty = dateOfBirth.value.isBlank()

                            showGenderError.value = genderEmpty
                            showFirstNameError.value = firstNameEmpty
                            showLastNameError.value = lastNameEmpty
                            showDateOfBirthError.value = dateOfBirthEmpty

                            val hasError =
                                genderEmpty ||
                                        firstNameEmpty ||
                                        lastNameEmpty ||
                                        dateOfBirthEmpty

                            if (!hasError) {
                                onSaveClick(
                                    photoUri.value,
                                    isPhotoRemoved,
                                    name.value,
                                    accountType.value,
                                    kroniqRole.value,
                                    gender.value,
                                    firstName.value,
                                    lastName.value,
                                    email.value,
                                    dateOfBirth.value
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    if (showPhotoActionSheet) {
        PhotoPickerOverlay(
            canDelete = photoUri.value != null || profileImageUrl.value.isNotBlank(),
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
                if (profileImageUrl.value.isNotBlank() || initialProfileImageUrl.isNotBlank()) {
                    profileImageUrl.value = ""
                    isPhotoRemoved = true
                }
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
                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
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

@RequiresApi(Build.VERSION_CODES.O)
private fun parseInitialDateOfBirth(dateOfBirth: String): LocalDate? {
    if (dateOfBirth.isBlank()) return null

    return runCatching {
        OffsetDateTime.parse(dateOfBirth).toLocalDate()
    }.getOrElse {
        runCatching {
            LocalDate.parse(dateOfBirth)
        }.getOrElse {
            runCatching {
            LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            }.getOrNull()
        }
    }
}
