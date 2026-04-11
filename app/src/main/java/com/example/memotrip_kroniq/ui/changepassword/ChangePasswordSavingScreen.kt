package com.example.memotrip_kroniq.ui.changepassword

import PreviewUiScaler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.data.remote.dto.ChangePasswordResponse
import com.example.memotrip_kroniq.ui.addtrip.screens.SavingTripScreen
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun ChangePasswordSavingScreen(
    currentPassword: String?,
    newPassword: String,
    changePassword: suspend (String?, String) -> ChangePasswordResponse,
    onFinished: () -> Unit,
    onFailed: (String?) -> Unit
) {
    LaunchedEffect(Unit) {
        runCatching {
            changePassword(currentPassword, newPassword)
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
private fun ChangePasswordSavingScreenPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            ChangePasswordSavingScreen(
                currentPassword = "oldPassword123",
                newPassword = "newPassword123",
                changePassword = { _, _ -> ChangePasswordResponse(success = true, hasPassword = true) },
                onFinished = {},
                onFailed = {}
            )
        }
    }
}
