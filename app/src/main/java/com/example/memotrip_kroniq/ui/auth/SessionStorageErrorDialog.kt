package com.example.memotrip_kroniq.ui.auth

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.memotrip_kroniq.R

@Composable
fun SessionStorageErrorDialog(onRetry: () -> Unit, onDismiss: (() -> Unit)? = null) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        text = { Text(stringResource(R.string.auth_storage_exit_error)) },
        confirmButton = { TextButton(onClick = onRetry) { Text(stringResource(R.string.auth_storage_retry)) } },
        dismissButton = if (onDismiss != null) {
            { TextButton(onClick = onDismiss) { Text(stringResource(R.string.auth_storage_exit_close)) } }
        } else null
    )
}
