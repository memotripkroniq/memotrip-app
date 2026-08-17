package com.example.memotrip_kroniq.ui.home.components.upsell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.home.HomePlanTier
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerTopRightShadow

@Composable
fun PlansTable(
    currentPlan: HomePlanTier,
    onGetPremiumClick: () -> Unit,
    onGetKroniqClick: () -> Unit,
    premiumEnabled: Boolean,
    kroniqEnabled: Boolean
) {
    val figmaTitleStrokeColor = Color(0x33000000) // 20% black
    val isFreeActive = currentPlan == HomePlanTier.FREE
    val isPremiumActive = currentPlan == HomePlanTier.PREMIUM
    val isKroniqActive = currentPlan == HomePlanTier.KRONIQ

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFA8C59E))
            .innerTopRightShadow(
                color = Color.Black.copy(alpha = 1.25f),
                topHeight = 6.dp,
                rightWidth = 6.dp,
                cornerRadius = 17.dp
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Top
    ) {
        PlanCard(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            title = "Free",
            useTitleImage = true,
            titleStrokeColor = figmaTitleStrokeColor,
            lines = listOf(
                stringResource(R.string.upsell_free_feature_1),
                stringResource(R.string.upsell_free_feature_2)
            ),
            price = null,
            buttonText = if (isFreeActive) {
                stringResource(R.string.upsell_button_active)
            } else {
                stringResource(R.string.upsell_plan_free)
            },
            backgroundColor = Color(0xFF383A41),
            strokeColor = Color(0xFF747781),
            innerShadowEnabled = true,
            buttonBackgroundColor = if (isFreeActive) Color(0xFF6F9E5E) else Color(0xFF383A41),
            onClick = null,
            buttonEnabled = false,
            isActive = isFreeActive
        )

        PlanCard(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            title = "Premium",
            useTitleImage = true,
            titleStrokeColor = figmaTitleStrokeColor,
            lines = listOf(
                stringResource(R.string.upsell_premium_feature_1),
                stringResource(R.string.upsell_premium_feature_2),
                stringResource(R.string.upsell_premium_feature_3),
                stringResource(R.string.upsell_premium_feature_4)
            ),
            price = "5.99 €",
            buttonText = if (isPremiumActive) {
                stringResource(R.string.upsell_button_active)
            } else {
                stringResource(R.string.upsell_button_get)
            },
            backgroundColor = Color(0xFF7BA065),
            strokeColor = Color(0xFF747781),
            innerShadowEnabled = true,
            buttonBackgroundColor = if (isPremiumActive) Color(0xFF6F9E5E) else Color(0xFF383A41),
            onClick = if (isPremiumActive) null else onGetPremiumClick,
            buttonEnabled = premiumEnabled && !isPremiumActive,
            showBadge = false,
            isActive = isPremiumActive
        )

        // ✅ KroniQ: místo textového buttonu dáme KroniqCtaButton
        PlanCard(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            title = "KroniQ",
            useTitleImage = true,
            titleStrokeColor = figmaTitleStrokeColor,
            lines = listOf(
                stringResource(R.string.upsell_kroniq_feature_1),
                stringResource(R.string.upsell_kroniq_feature_2),
                stringResource(R.string.upsell_kroniq_feature_3),
                stringResource(R.string.upsell_kroniq_feature_4),
                stringResource(R.string.upsell_kroniq_feature_5),
                stringResource(R.string.upsell_kroniq_feature_6)
            ),
            price = "36 €",
            buttonText = if (isKroniqActive) {
                stringResource(R.string.upsell_button_active)
            } else {
                stringResource(R.string.upsell_button_get)
            },
            backgroundColor = Color(0xFF4E6E3E),
            strokeColor = Color(0xFF747781),
            innerShadowEnabled = true,
            buttonBackgroundColor = if (isKroniqActive) Color(0xFF6F9E5E) else Color(0xFF383A41),
            onClick = if (isKroniqActive) null else onGetKroniqClick,
            buttonEnabled = kroniqEnabled && !isKroniqActive,
            showBadge = !isKroniqActive,
            isActive = isKroniqActive
        )
    }
}
