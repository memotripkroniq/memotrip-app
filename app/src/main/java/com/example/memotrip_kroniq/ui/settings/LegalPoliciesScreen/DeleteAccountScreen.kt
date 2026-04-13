package com.example.memotrip_kroniq.ui.settings

import PreviewUiScaler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.ui.auth.authMessage
import com.example.memotrip_kroniq.ui.auth.google.GoogleAuthManager
import com.example.memotrip_kroniq.ui.components.CustomInputBox
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import kotlinx.coroutines.launch

@Composable
fun DeleteAccountScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val tokenStore = remember { TokenDataStore(context) }
    val authRepository = remember {
        AuthRepository(
            api = RetrofitClient.authApi,
            tokenStore = tokenStore
        )
    }
    val googleAuthManager = remember { GoogleAuthManager(context) }

    var hasPassword by remember { mutableStateOf<Boolean?>(null) }
    var currentPassword by remember { mutableStateOf("") }
    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    val localizedCurrentPasswordError = authMessage(currentPasswordError) ?: currentPasswordError
    val localizedGeneralError = authMessage(generalError) ?: generalError

    LaunchedEffect(authRepository) {
        hasPassword = runCatching { authRepository.getMe().hasPassword }
            .getOrDefault(true)
    }

    val googleDeleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val data = result.data
        val token = data?.let { googleAuthManager.extractGoogleToken(it) }
        if (token.isNullOrBlank()) {
            isDeleting = false
            generalError = context.getString(R.string.delete_account_error_google_required)
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            runDeleteAccount(
                repository = authRepository,
                tokenStore = tokenStore,
                navController = navController,
                onStart = {
                    isDeleting = true
                    generalError = null
                },
                onError = {
                    isDeleting = false
                    generalError = mapDeleteAccountError(context, it)
                },
                googleIdToken = token
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }
    ) {
        AppTopBar(
            title = stringResource(R.string.legal_delete_account),
            showBack = true,
            onBackClick = { navController.popBackStack() },
            onMenuClick = null
        )

        if (hasPassword == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = stringResource(R.string.delete_account_intro_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.delete_account_intro_body),
                    color = Color.White.copy(alpha = 0.86f)
                )

                DeleteAccountSection(
                    title = stringResource(R.string.delete_account_data_title),
                    body = stringResource(R.string.delete_account_data_body)
                )

                DeleteAccountSection(
                    title = stringResource(R.string.delete_account_effects_title),
                    body = stringResource(R.string.delete_account_effects_body)
                )

                DeleteAccountSection(
                    title = stringResource(R.string.delete_account_current_status_title),
                    body = stringResource(R.string.delete_account_reauth_body)
                )

                DeleteAccountSection(
                    title = stringResource(R.string.delete_account_note_title),
                    body = stringResource(R.string.delete_account_note_body)
                )

                if (hasPassword == true) {
                    Text(
                        text = stringResource(R.string.delete_account_password_prompt),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    CustomInputBox(
                        label = stringResource(R.string.delete_account_password_placeholder),
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            currentPasswordError = null
                            generalError = null
                        },
                        isPassword = true,
                        error = localizedCurrentPasswordError,
                        showError = true,
                        onFocus = {
                            currentPasswordError = null
                            generalError = null
                        }
                    )

                    PrimaryButton(
                        text = stringResource(R.string.delete_account_button),
                        enabled = !isDeleting,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            focusManager.clearFocus()
                            currentPasswordError = null
                            generalError = null

                            if (currentPassword.isBlank()) {
                                currentPasswordError =
                                    context.getString(R.string.delete_account_error_password_required)
                                return@PrimaryButton
                            }

                            coroutineScope.launch {
                                runDeleteAccount(
                                    repository = authRepository,
                                    tokenStore = tokenStore,
                                    navController = navController,
                                    onStart = { isDeleting = true },
                                    onError = {
                                        isDeleting = false
                                        when {
                                            it.contains("INVALID_PASSWORD", ignoreCase = true) ||
                                                it.contains("Incorrect current password", ignoreCase = true) ->
                                                currentPasswordError =
                                                    context.getString(R.string.delete_account_error_invalid_password)
                                            else ->
                                                generalError = mapDeleteAccountError(context, it)
                                        }
                                    },
                                    currentPassword = currentPassword
                                )
                            }
                        }
                    )
                } else {
                    Text(
                        text = stringResource(R.string.delete_account_google_prompt),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    PrimaryButton(
                        text = stringResource(R.string.delete_account_google_button),
                        enabled = !isDeleting,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            focusManager.clearFocus()
                            generalError = null
                            isDeleting = true
                            googleAuthManager.beginSignIn(googleDeleteLauncher)
                        }
                    )
                }

                if (localizedGeneralError != null) {
                    Text(
                        text = localizedGeneralError,
                        color = Color(0xFF759F67)
                    )
                }

                if (isDeleting) {
                    CircularProgressIndicator(color = Color.White)
                }

                Text(
                    text = stringResource(R.string.delete_account_footer_note),
                    color = Color.White.copy(alpha = 0.62f)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DeleteAccountSection(
    title: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = body,
            color = Color.White.copy(alpha = 0.82f)
        )
    }
}

private suspend fun runDeleteAccount(
    repository: AuthRepository,
    tokenStore: TokenDataStore,
    navController: NavHostController,
    onStart: () -> Unit,
    onError: (String) -> Unit,
    currentPassword: String? = null,
    googleIdToken: String? = null
) {
    onStart()

    runCatching {
        repository.deleteAccount(
            currentPassword = currentPassword,
            googleIdToken = googleIdToken
        )
    }.onSuccess {
        tokenStore.clearTokens()
        navController.navigate(com.example.memotrip_kroniq.navigation.Screen.Login.route) {
            popUpTo(0)
            launchSingleTop = true
        }
    }.onFailure {
        onError(it.message ?: "Delete account failed")
    }
}

private fun mapDeleteAccountError(
    context: android.content.Context,
    message: String
): String {
    return when {
        message.contains("INVALID_PASSWORD", ignoreCase = true) ->
            context.getString(R.string.delete_account_error_invalid_password)
        message.contains("GOOGLE_REAUTH_REQUIRED", ignoreCase = true) ->
            context.getString(R.string.delete_account_error_google_required)
        message.contains("RECENT_REAUTH_REQUIRED", ignoreCase = true) ->
            context.getString(R.string.delete_account_error_reauth_required)
        message.contains("DELETE_NOT_ALLOWED", ignoreCase = true) ->
            context.getString(R.string.delete_account_error_not_allowed)
        else -> message
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun DeleteAccountScreenPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            DeleteAccountScreen(
                navController = rememberNavController()
            )
        }
    }
}
