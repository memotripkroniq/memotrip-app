package com.example.memotrip_kroniq.ui.home.components.upsell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerTopShadow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KroniqCtaButton(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color,
    enabled: Boolean = true,
    showBadge: Boolean = true,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    // Reserve real space for each child. On narrow cards (or larger font scales),
    // wrap the badge below the label rather than drawing it over the label.
    FlowRow(
        modifier = modifier
            .alpha(if (enabled || isActive) 1f else 0.65f)
            .fillMaxWidth()
            .heightIn(min = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .innerTopShadow(
                alpha = 0.35f,
                height = 15f
            )
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 3.dp, vertical = 2.dp)
        )

        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFA8C59E))
                    .innerTopShadow(
                        alpha = 0.25f,
                        height = 15f
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.upsell_badge_50_off),
                    color = Color(0xFF2E3037),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
