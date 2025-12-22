package com.example.memotrip_kroniq.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx

@Composable
fun MiniRadioButton(
    selected: Boolean,
    onClick: () -> Unit
) {
    val s = LocalUiScaler.current

    Canvas(
        modifier = Modifier
            .size(15f.sx(s))             // 🔥 skutečná velikost
            .clickable { onClick() }
    ) {

        // OUTLINE circle
        drawCircle(
            color = Color.White,
            radius = size.minDimension / 2,
            style = Stroke(width = 2f)
        )

        // INNER dot (když selected = true)
        if (selected) {
            drawCircle(
                color = Color(0xFF0077C8), // tvoje modrá
                radius = size.minDimension / 2.5f,
                center = Offset(size.width / 2, size.height / 2)
            )
        }
    }
}
