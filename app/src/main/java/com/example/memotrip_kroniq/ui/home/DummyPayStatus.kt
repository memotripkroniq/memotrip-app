package com.example.memotrip_kroniq.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.memotrip_kroniq.R

@Composable
internal fun DummyPayStatus(
    state: HomeUiState,
    onVerify: () -> Unit,
    onReopen: () -> Unit,
    onDismissPending: () -> Unit,
    onShowPending: () -> Unit
) {
    if (state.dummyPayState == DummyPayState.WAITING || state.isDummyPayNoticeCollapsed) {
        if (state.isDummyPayNoticeCollapsed) {
            // Remains reachable on every Home tab, even without trips or an upsell table.
            TextButton(
                onClick = onShowPending,
                enabled = state.dummyPayState == DummyPayState.WAITING,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(stringResource(R.string.dummy_pay_pending_indicator))
            }
        } else {
            PendingDummyPayBanner(state.canReopenDummyPay, onVerify, onReopen, onDismissPending)
        }
        return
    }
    val message = when (state.dummyPayState) {
        DummyPayState.IDLE -> {
            if (state.isLoading || state.isAccountVerified) return
            R.string.dummy_pay_verification_error
        }
        DummyPayState.CREATING -> R.string.dummy_pay_creating
        DummyPayState.WAITING -> R.string.dummy_pay_waiting
        DummyPayState.VERIFYING -> R.string.dummy_pay_verifying
        // Terminal results use one-time Snackbars; actionable states remain cards.
        DummyPayState.CONFIRMED, DummyPayState.FAILED -> return
        DummyPayState.UNCONFIRMED -> R.string.dummy_pay_unconfirmed
        DummyPayState.VERIFICATION_ERROR -> R.string.dummy_pay_verification_error
        DummyPayState.OPEN_ERROR -> R.string.dummy_pay_open_error
        DummyPayState.CREATION_UNKNOWN -> R.string.dummy_pay_creation_unknown
        DummyPayState.START_ERROR -> R.string.dummy_pay_start_error
        DummyPayState.UNAVAILABLE -> R.string.upsell_dummy_pay_unavailable
    }
    val busy = state.dummyPayState in setOf(DummyPayState.CREATING, DummyPayState.VERIFYING)
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState()).padding(12.dp)) {
            Text(stringResource(message))
            if (!busy && (state.dummyPayPlan != null || !state.isAccountVerified) &&
                state.dummyPayState != DummyPayState.CONFIRMED) {
                TextButton(onClick = onVerify) { Text(stringResource(R.string.dummy_pay_verify_again)) }
                if (state.canReopenDummyPay) {
                    TextButton(onClick = onReopen) { Text(stringResource(R.string.dummy_pay_reopen)) }
                } else if (state.dummyPayPlan != null) {
                    Text(stringResource(R.string.dummy_pay_url_unavailable))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PendingDummyPayBanner(
    canReopen: Boolean,
    onVerify: () -> Unit,
    onReopen: () -> Unit,
    onDismiss: () -> Unit
) {
    val hideLabel = stringResource(R.string.dummy_pay_hide_notice)
    val actionColors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.inversePrimary)
    Snackbar(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        actionOnNewLine = true,
        dismissAction = {
            IconButton(onClick = onDismiss) {
                Text("×", fontSize = 22.sp, modifier = Modifier.semantics { contentDescription = hideLabel })
            }
        },
        action = {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onVerify, colors = actionColors) {
                    Text(stringResource(R.string.dummy_pay_verify_again))
                }
                if (canReopen) {
                    TextButton(onClick = onReopen, colors = actionColors) {
                        Text(stringResource(R.string.dummy_pay_reopen))
                    }
                }
            }
        }
    ) {
        Text(stringResource(R.string.dummy_pay_pending_title))
    }
}
