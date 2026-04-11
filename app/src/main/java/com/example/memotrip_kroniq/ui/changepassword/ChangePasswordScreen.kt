package com.example.memotrip_kroniq.ui.changepassword

import PreviewUiScaler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.auth.Validators
import com.example.memotrip_kroniq.ui.auth.authMessage
import com.example.memotrip_kroniq.ui.components.CustomInputBox
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun ChangePasswordScreen(
    hasPassword: Boolean,
    backendErrorMessage: String? = null,
    onBackendErrorConsumed: () -> Unit = {},
    onBack: () -> Unit = {},
    onContinue: (currentPassword: String?, newPassword: String) -> Unit = { _, _ -> }
) {
    val s = LocalUiScaler.current
    val focusManager = LocalFocusManager.current

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }

    val localizedOldPasswordError =
        oldPasswordError?.let {
            if (it == OLD_PASSWORD_REQUIRED) stringResource(R.string.change_password_error_old_password_required)
            else authMessage(it) ?: it
        }
    val localizedNewPasswordError = authMessage(newPasswordError)
    val localizedConfirmPasswordError = authMessage(confirmPasswordError)
    val localizedGeneralError = authMessage(generalError)

    LaunchedEffect(backendErrorMessage, hasPassword) {
        if (backendErrorMessage == null) return@LaunchedEffect

        val normalized = backendErrorMessage.lowercase()
        when {
            hasPassword && (
                normalized.contains("current password") ||
                    normalized.contains("old password") ||
                    normalized.contains("incorrect password") ||
                    normalized.contains("invalid password")
                ) -> oldPasswordError = mapCurrentPasswordError(backendErrorMessage)

            normalized.contains("new password") ||
                normalized.contains("password too") ||
                normalized.contains("minimum") ||
                normalized.contains("must contain") ->
                newPasswordError = backendErrorMessage

            else -> generalError = backendErrorMessage
        }

        submitted = true
        onBackendErrorConsumed()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                title = stringResource(R.string.change_password_title),
                showBack = true,
                onBackClick = onBack,
                onMenuClick = null
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36f.sx(s)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(18f.sy(s)))

                TextBlock(
                    title = stringResource(
                        if (hasPassword) {
                            R.string.change_password_heading
                        } else {
                            R.string.change_password_create_heading
                        }
                    )
                )

                Spacer(modifier = Modifier.height(38f.sy(s)))

                if (hasPassword) {
                    PasswordField(
                        label = stringResource(R.string.change_password_old_password),
                        placeholder = stringResource(R.string.change_password_old_password_placeholder),
                        value = oldPassword,
                        error = if (submitted) localizedOldPasswordError else null,
                        onValueChange = {
                        oldPassword = it
                        if (submitted) oldPasswordError = null
                        generalError = null
                    }
                )

                    Spacer(modifier = Modifier.height(24f.sy(s)))
                }

                PasswordField(
                    label = stringResource(R.string.change_password_new_password),
                    placeholder = stringResource(R.string.change_password_new_password_placeholder),
                    value = newPassword,
                    error = if (submitted) localizedNewPasswordError else null,
                    onValueChange = {
                        newPassword = it
                        if (submitted) newPasswordError = null
                        generalError = null
                    }
                )

                Spacer(modifier = Modifier.height(24f.sy(s)))

                PasswordField(
                    label = stringResource(R.string.change_password_confirm_new_password),
                    placeholder = stringResource(R.string.change_password_confirm_new_password_placeholder),
                    value = confirmPassword,
                    error = if (submitted) localizedConfirmPasswordError else null,
                    onValueChange = {
                        confirmPassword = it
                        if (submitted) confirmPasswordError = null
                        generalError = null
                    }
                )

                Spacer(modifier = Modifier.height(30f.sy(s)))

                PrimaryButton(
                    text = stringResource(R.string.auth_continue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40f.sy(s)),
                    onClick = {
                        submitted = true

                        oldPasswordError = if (hasPassword && oldPassword.isBlank()) OLD_PASSWORD_REQUIRED else null
                        newPasswordError = Validators.validatePassword(newPassword)
                        confirmPasswordError = when {
                            confirmPassword.isBlank() -> "Confirm password is required"
                            !Validators.doPasswordsMatch(newPassword, confirmPassword) -> "Passwords do not match"
                            else -> null
                        }
                        generalError = null

                        if (
                            oldPasswordError == null &&
                            newPasswordError == null &&
                            confirmPasswordError == null
                        ) {
                            focusManager.clearFocus()
                            onContinue(
                                oldPassword.takeIf { hasPassword },
                                newPassword
                            )
                        }
                    }
                )

                if (submitted && localizedGeneralError != null) {
                    Spacer(modifier = Modifier.height(16f.sy(s)))
                    androidx.compose.material3.Text(
                        text = localizedGeneralError,
                        color = Color(0xFF759F67),
                        fontSize = 14f.fs(s),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun TextBlock(title: String) {
    val s = LocalUiScaler.current

    androidx.compose.material3.Text(
        text = title,
        fontSize = 32f.fs(s),
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun PasswordField(
    label: String,
    placeholder: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit
) {
    val s = LocalUiScaler.current

    androidx.compose.material3.Text(
        text = label,
        fontSize = 16f.fs(s),
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8f.sy(s))
    )

    CustomInputBox(
        label = placeholder,
        value = value,
        onValueChange = onValueChange,
        isPassword = true,
        error = error,
        showError = error != null
    )
}

private const val OLD_PASSWORD_REQUIRED = "Old password is required"

private fun mapCurrentPasswordError(message: String): String =
    when {
        message.equals("Incorrect current password", ignoreCase = true) -> "Incorrect current password"
        message.equals("Incorrect old password", ignoreCase = true) -> "Incorrect current password"
        else -> message
    }

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 412, heightDp = 892)
@Composable
private fun ChangePasswordScreenPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            ChangePasswordScreen(hasPassword = true)
        }
    }
}
