package com.example.memotrip_kroniq.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.ui.addtrip.screens.SavingTripScreen
import com.example.memotrip_kroniq.ui.changepassword.ChangePasswordScreen
import com.example.memotrip_kroniq.ui.changepassword.ChangePasswordSuccessScreen
import com.memotrip_kroniq.R

internal const val DEV_RESET_ROUTE = "dev_reset_password"

@Composable
internal fun DevResetPasswordScreen(model: ResetPasswordLinkViewModel, onExit: () -> Unit) {
    val state by model.state.collectAsState()
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { AuthRepository(RetrofitClient.authApi, TokenDataStore(context)) }
    BackHandler(onBack = onExit)
    // A new link discards all remembered password fields, even if it has the same token.
    key(state.generation) {
        when {
            state.completed -> ChangePasswordSuccessScreen(onFinished = onExit)
            state.saving -> SavingTripScreen(stringResource(R.string.change_password_saving))
            !state.valid -> Column(Modifier.padding(24.dp)) {
                Text(stringResource(R.string.reset_password_error_invalid_link))
                TextButton(onClick = onExit) { Text(stringResource(R.string.common_back)) }
            }
            else -> ChangePasswordScreen(
                hasPassword = false,
                titleRes = R.string.reset_password_title,
                headingWithoutPasswordRes = R.string.reset_password_heading,
                backendErrorMessage = if (state.failed) stringResource(R.string.auth_error_network_or_server) else null,
                onBack = onExit,
                onContinue = { _, password -> model.submit(password, repository::resetPassword) }
            )
        }
    }
}
