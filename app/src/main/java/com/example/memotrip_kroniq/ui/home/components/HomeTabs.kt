package com.example.memotrip_kroniq.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.HomeTab
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerTopShadow


@Composable
fun HomeTabs(
    selected: HomeTab,
    isThemesLocked: Boolean,
    onToggleLock: () -> Unit,
    onTabSelected: (HomeTab) -> Unit
) {
    val s = LocalUiScaler.current
    val containerColor = Color(0xFF383A41)
    val containerGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF383A41).copy(alpha = 0.55f),
            Color(0xFF383A41).copy(alpha = 0.55f)
        )
    )
    val borderColor = Color(0xFF747781)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50f.sy(s))
            .background(containerColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(
                brush = containerGradient,
                shape = RoundedCornerShape(10.dp))
            .innerTopShadow(
                alpha = 0.18f,
                height = 18f
            )
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        HomeTabButton(
            title = "Themes",
            selected = selected == HomeTab.THEMES,
            showLock = isThemesLocked,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (selected == HomeTab.THEMES)
                        Modifier.innerTopShadow(alpha = 0.16f, height = 20f)
                    else
                        Modifier
                ),
            onClick = {
                onTabSelected(HomeTab.THEMES)
                onToggleLock()        // ← přepíná stav lock/unlock
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        HomeTabButton(
            title = "Trip history",
            selected = selected == HomeTab.TRIP_HISTORY,
            showLock = false,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (selected == HomeTab.TRIP_HISTORY)
                        Modifier.innerTopShadow(alpha = 0.16f, height = 20f)
                    else
                        Modifier
                ),
            onClick = { onTabSelected(HomeTab.TRIP_HISTORY) }
        )
    }
}

@Composable
private fun HomeTabButton(
    title: String,
    selected: Boolean,
    showLock: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val s = LocalUiScaler.current
    val bg = if (selected) Color(0xFF0077C8) else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxHeight()
            .background(bg, RoundedCornerShape(8.dp))
            .clickable(
                indication = null,                        // ❌ vypne ripple
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 16f.sx(s)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = Color.White,
            fontSize = 18f.fs(s),
            fontWeight = FontWeight.Bold
        )

        if (showLock) {
            Spacer(modifier = Modifier.width(10f.sx(s)))
            Image(
                painter = painterResource(R.drawable.homescreen_ic_lock_theme),
                contentDescription = null,
                modifier = Modifier.size(18f.sx(s))
            )
        }
    }
}
