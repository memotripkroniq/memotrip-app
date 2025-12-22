package com.example.memotrip_kroniq.ui.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.BoxWithConstraints

@Composable
fun ScaledLayout(
    baselineWidth: Float = 412f,
    baselineHeight: Float = 892f,
    content: @Composable (scale: Float) -> Unit
) {
    BoxWithConstraints {

        val scaleW = maxWidth.value / baselineWidth
        val scaleH = maxHeight.value / baselineHeight

        // používáme menší scale → UI se „vejde“
        val scale = kotlin.math.min(scaleW, scaleH)

        content(scale)
    }
}

// helper pro přepočet px → dp * scale
fun Float.s(scale: Float): Dp = (this * scale).dp
