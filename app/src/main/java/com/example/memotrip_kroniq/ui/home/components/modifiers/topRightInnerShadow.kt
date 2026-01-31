package com.example.memotrip_kroniq.ui.home.components.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.innerTopRightShadow(
    color: Color = Color.Black.copy(alpha = 0.25f),
    topHeight: Dp = 6.dp,
    rightWidth: Dp = 6.dp,
    cornerRadius: Dp = 12.dp   // 👈 musí odpovídat RoundedCornerShape
) = this.drawBehind {

    val topPx = topHeight.toPx()
    val rightPx = rightWidth.toPx()
    val cornerPx = cornerRadius.toPx()

    // 🔝 TOP inner shadow
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(color, Color.Transparent),
            startY = 0f,
            endY = topPx
        )
    )

    // 👉 RIGHT inner shadow
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, color),
            startX = size.width - rightPx,
            endX = size.width
        )
    )

    // 🟢 CORNER BOOST (pravý horní roh)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = Offset(size.width, 0f),
            radius = cornerPx
        ),
        radius = cornerPx,
        center = Offset(size.width, 0f)
    )
}

