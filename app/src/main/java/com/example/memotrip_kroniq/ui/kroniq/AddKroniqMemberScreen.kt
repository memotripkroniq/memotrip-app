package com.example.memotrip_kroniq.ui.kroniq

import PreviewUiScaler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.components.CustomInputBox
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun AddKroniqMemberScreen(
    backendErrorMessage: String? = null,
    onBackendErrorConsumed: () -> Unit = {},
    title: String = stringResource(R.string.kroniq_add_member_title),
    buttonText: String = stringResource(R.string.kroniq_add_member_button),
    onBack: () -> Unit,
    onAddMember: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    val requiredError = stringResource(R.string.kroniq_add_member_error_required)
    val invalidError = stringResource(R.string.kroniq_add_member_error_invalid)

    LaunchedEffect(backendErrorMessage) {
        if (!backendErrorMessage.isNullOrBlank()) {
            focusManager.clearFocus(force = true)
            isSubmitting = false
            email = ""
            emailError = backendErrorMessage
            submitted = true
            onBackendErrorConsumed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                title = title,
                showBack = true,
                onBackClick = onBack,
                onMenuClick = null
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(72.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.kroniq_add_email_label),
                        color = Color.White,
                        fontSize = 14f.fs(LocalUiScaler.current)
                    )

                    CustomInputBox(
                        label = stringResource(R.string.kroniq_add_email_placeholder),
                        value = email,
                        onValueChange = {
                            email = it
                            isSubmitting = false
                            emailError = null
                        },
                        error = if (submitted) emailError else null,
                        showError = submitted,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                PrimaryButton(
                    text = buttonText,
                    onClick = {
                        if (isSubmitting) return@PrimaryButton

                        focusManager.clearFocus(force = true)
                        val normalizedEmail = email.trim()
                        submitted = true
                        emailError = when {
                            normalizedEmail.isBlank() -> requiredError
                            !isStrictEmail(normalizedEmail) -> invalidError
                            else -> null
                        }

                        if (emailError == null) {
                            isSubmitting = true
                            onAddMember(normalizedEmail)
                        }
                    }
                )
            }
        }
    }
}

private fun isStrictEmail(value: String): Boolean {
    if (value.contains("..")) return false

    val parts = value.split("@")
    if (parts.size != 2) return false

    val localPart = parts[0]
    val domainPart = parts[1]

    if (localPart.isBlank() || domainPart.isBlank()) return false
    if (localPart.startsWith(".") || localPart.endsWith(".")) return false
    if (domainPart.startsWith(".") || domainPart.endsWith(".")) return false

    val localRegex = Regex("^[A-Za-z0-9+_%\\-](?:[A-Za-z0-9+_.%\\-]*[A-Za-z0-9+_%\\-])?$")
    if (!localRegex.matches(localPart)) return false

    val domainLabels = domainPart.split(".")
    if (domainLabels.size < 2) return false

    val domainLabelRegex = Regex("^[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?$")
    if (domainLabels.any { it.isBlank() || !domainLabelRegex.matches(it) }) return false

    val tld = domainLabels.last()
    return tld.length >= 2 && tld.all { it.isLetter() }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 412, heightDp = 892)
@Composable
private fun AddKroniqMemberScreenPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            AddKroniqMemberScreen(
                title = stringResource(R.string.kroniq_add_guest_title),
                buttonText = stringResource(R.string.kroniq_add_guest_button),
                onBack = {},
                onAddMember = {}
            )
        }
    }
}
