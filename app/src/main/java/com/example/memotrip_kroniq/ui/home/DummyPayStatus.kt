package com.example.memotrip_kroniq.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import com.example.memotrip_kroniq.ui.core.topOverlayShadow
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.memotrip_kroniq.R

@Composable
internal fun DummyPayStatus(
    state: HomeUiState,
    onVerify: () -> Unit,
    onReopen: () -> Unit,
    onCancel: () -> Unit,
    onDismissPending: () -> Unit,
    onShowPending: () -> Unit
) {
    if (state.isPendingPaymentNotice) {
        if (state.isDummyPayNoticeCollapsed) {
            TextButton(
                onClick = onShowPending,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(stringResource(R.string.dummy_pay_pending_indicator), style = MaterialTheme.typography.labelMedium)
            }
        } else {
            PendingDummyPayPanel(
                verifying = state.dummyPayState == DummyPayState.VERIFYING,
                hasPurchase = state.dummyPayPurchaseId != null,
                onVerify = onVerify, onContinue = onReopen, onCancel = onCancel, onDismiss = onDismissPending
            )
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
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.small
    ) {
        Column(Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState()).padding(12.dp)) {
            Text(stringResource(message))
            if (!busy && (state.dummyPayPlan != null || !state.isAccountVerified) &&
                state.dummyPayState != DummyPayState.CONFIRMED) {
                TextButton(onClick = onVerify) { Text(stringResource(R.string.dummy_pay_verify_again)) }
                if (state.dummyPayPurchaseId != null) {
                    TextButton(onClick = onReopen) { Text(stringResource(R.string.dummy_pay_continue)) }
                    TextButton(onClick = onCancel) { Text(stringResource(R.string.dummy_pay_cancel)) }
                } else if (state.dummyPayPlan != null) {
                    Text(stringResource(R.string.dummy_pay_url_unavailable))
                }
            }
        }
    }
}

@Composable
private fun PendingDummyPayPanel(
    verifying: Boolean,
    hasPurchase: Boolean,
    onVerify: () -> Unit,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val title = stringResource(R.string.dummy_pay_pending_title)
    val description = stringResource(R.string.dummy_pay_pending_subtitle)
    val verifyLabel = stringResource(R.string.dummy_pay_verify_again)
    val hideLabel = stringResource(R.string.dummy_pay_hide_notice)
    val stateLabel = stringResource(if (verifying) R.string.dummy_pay_verifying_short else R.string.dummy_pay_pending_title)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        color = colors.surface,
        contentColor = colors.onSurface,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.12f)),
        shadowElevation = 0.dp, tonalElevation = 0.dp
    ) {
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(20.dp).clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
                    if (verifying) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 1.5.dp,
                            color = colors.onSurface.copy(alpha = 0.65f))
                    } else {
                        val iconColor = colors.onSurface.copy(alpha = 0.65f)
                        Canvas(Modifier.size(18.dp)) {
                            drawCircle(iconColor, style = Stroke(1.4.dp.toPx()))
                            drawLine(iconColor, center, Offset(center.x, center.y - size.height * 0.23f),
                                strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
                            drawLine(iconColor, center, Offset(center.x + size.width * 0.2f, center.y),
                                strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f).semantics {
                        liveRegion = LiveRegionMode.Polite
                        stateDescription = stateLabel
                        contentDescription = "$title. $description"
                    })
                IconButton(onClick = onVerify, enabled = !verifying,
                    modifier = Modifier.semantics { contentDescription = verifyLabel }) {
                    Text("↻", fontSize = 22.sp, modifier = Modifier.clearAndSetSemantics {})
                }
                IconButton(onClick = onDismiss, modifier = Modifier.semantics { contentDescription = hideLabel }) {
                    Text("×", fontSize = 22.sp, modifier = Modifier.clearAndSetSemantics {})
                }
            }
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val density = LocalDensity.current
                val enabled = hasPurchase && !verifying
                val continueLabel = stringResource(R.string.dummy_pay_continue)
                val cancelLabel = stringResource(R.string.dummy_pay_cancel)
                val textMeasurer = rememberTextMeasurer()
                val buttonWidth = (maxWidth - 8.dp) / 2
                val textConstraints = Constraints(maxWidth = with(density) {
                    (buttonWidth - 16.dp).roundToPx().coerceAtLeast(1)
                })
                // Keep the 48 dp interactive row, but render a smaller surface inside it.
                // Both visual surfaces grow together when translated/scaled text needs room.
                val continueHeight = textMeasurer.measure(continueLabel,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    constraints = textConstraints).size.height
                val cancelHeight = textMeasurer.measure(cancelLabel,
                    style = MaterialTheme.typography.labelLarge, constraints = textConstraints).size.height
                val visualHeight = (with(density) { maxOf(continueHeight, cancelHeight).toDp() } + 6.dp)
                    .coerceAtLeast(36.dp)
                val touchHeight = visualHeight.coerceAtLeast(48.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PendingPaymentAction(continueLabel, onContinue, enabled, primary = true,
                        visualHeight = visualHeight, touchHeight = touchHeight, modifier = Modifier.weight(1f))
                    PendingPaymentAction(cancelLabel, onCancel, enabled, primary = false,
                        visualHeight = visualHeight, touchHeight = touchHeight, modifier = Modifier.weight(1f))
                }
            }

        }
    }
}

// The outer node owns clicks, focus and button semantics across the full 48 dp target.
// The inner, non-interactive surface only draws the compact background, border and text.
@Composable
private fun PendingPaymentAction(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    primary: Boolean,
    visualHeight: Dp,
    touchHeight: Dp,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val shape = if (primary) RoundedCornerShape(8.dp) else MaterialTheme.shapes.small
    // Match the existing PrimaryButton and OutlinedButton palettes, including disabled states.
    val buttonColors = if (primary) ButtonDefaults.buttonColors(
        containerColor = colors.primary, disabledContainerColor = Color(0xFF005A9E)
    ) else ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface)
    Box(
        modifier = modifier.height(touchHeight).clickable(
            enabled = enabled, role = Role.Button, interactionSource = interactionSource,
            indication = null, onClick = onClick
        ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(visualHeight).clip(shape)
                .indication(interactionSource, LocalIndication.current)
                .then(if (primary) Modifier.topOverlayShadow() else Modifier),
            shape = shape,
            color = if (enabled) buttonColors.containerColor else buttonColors.disabledContainerColor,
            contentColor = if (enabled) buttonColors.contentColor else buttonColors.disabledContentColor,
            border = if (primary) null else BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.2f)),
            tonalElevation = 0.dp, shadowElevation = 0.dp
        ) {
            Box(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), contentAlignment = Alignment.Center) {
                Text(text, style = if (primary) MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp, fontWeight = FontWeight.Bold
                ) else MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
            }
        }
    }
}
