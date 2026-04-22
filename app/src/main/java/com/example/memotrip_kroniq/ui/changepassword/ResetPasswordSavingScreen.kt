package com.example.memotrip_kroniq.ui.changepassword

import PreviewUiScaler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.addtrip.screens.SavingTripScreen
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun ResetPasswordSavingScreen(
    token: String,
    newPassword: String,
    resetPassword: suspend (String, String) -> Unit,
    onFinished: () -> Unit,
    onFailed: (String?) -> Unit
) {
    LaunchedEffect(Unit) {
        runCatching {
            resetPassword(token, newPassword)
        }.onSuccess {
            onFinished()
        }.onFailure {
            onFailed(it.message)
        }
    }

    SavingTripScreen(
        message = androidx.compose.ui.res.stringResource(R.string.change_password_saving)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 412, heightDp = 892)
@Composable
private fun ResetPasswordSavingScreenPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            ResetPasswordSavingScreen(
                token = "preview-token",
                newPassword = "newPassword123",
                resetPassword = { _, _ -> },
                onFinished = {},
                onFailed = {}
            )
        }
    }
}
