package com.example.memotrip_kroniq.ui.home.components.modifiers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.planButtonTopInnerShadow(
    backgroundColor: Color,
    onClick: (() -> Unit)?
): Modifier {
    return this
        .fillMaxWidth()
        .height(24.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(backgroundColor)
        .innerTopShadow(
            alpha = 0.35f,
            height = 15f          // 👈 jemný horní stín
        )
        .let {
            if (onClick != null) {
                it.clickable(
                    indication = null,
                    interactionSource = MutableInteractionSource()
                ) { onClick() }
            } else it
        }
}