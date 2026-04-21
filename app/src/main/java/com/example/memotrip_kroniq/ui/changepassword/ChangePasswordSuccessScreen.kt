package com.example.memotrip_kroniq.ui.changepassword

import PreviewUiScaler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.addtrip.screens.TripSuccessContent
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import kotlinx.coroutines.delay

@Composable
fun ChangePasswordSuccessScreen(
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1500)
        onFinished()
    }

    TripSuccessContent(
        title = androidx.compose.ui.res.stringResource(R.string.change_password_success_title),
        subtitle = androidx.compose.ui.res.stringResource(R.string.change_password_success_subtitle),
        footer = androidx.compose.ui.res.stringResource(R.string.change_password_success_footer)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 412, heightDp = 892)
@Composable
private fun ChangePasswordSuccessScreenPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            ChangePasswordSuccessScreen(onFinished = {})
        }
    }
}
