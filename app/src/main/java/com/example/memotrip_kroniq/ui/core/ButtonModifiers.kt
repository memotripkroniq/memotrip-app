package com.example.memotrip_kroniq.ui.core

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Modifier.topOverlayShadow(
    alpha: Float = 0.24f,
    heightDp: Float = 10f
): Modifier = this.drawWithContent {
    drawContent()

    val shadowHeightPx = heightDp * density
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(alpha = alpha),
                Color.Transparent
            ),
            startY = 0f,
            endY = shadowHeightPx
        ),
        size = size
    )
}
