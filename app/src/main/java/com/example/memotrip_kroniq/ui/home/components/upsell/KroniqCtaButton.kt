package com.example.memotrip_kroniq.ui.home.components.upsell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerTopShadow

@Composable
fun KroniqCtaButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF383A41))
            .innerTopShadow(
                alpha = 0.35f,
                height = 15f
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            // ✅ padding jen zleva (vpravo 0dp -> badge může být nalepený)
            .padding(start = 8.dp, end = 3.dp),
        verticalAlignment = Alignment.CenterVertically
        // ❌ žádné spacedBy
    ) {
        Text(
            text = stringResource(R.string.upsell_button_get),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )

        Spacer(modifier = Modifier.weight(1f)) // ✅ vytlačí badge doprava

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFA8C59E))
                .innerTopShadow(
                    alpha = 0.25f,
                    height = 15f
                )
                // ✅ menší badge, aby byl vidět šedý okraj kolem
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.upsell_badge_50_off),
                color = Color(0xFF2E3037),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
