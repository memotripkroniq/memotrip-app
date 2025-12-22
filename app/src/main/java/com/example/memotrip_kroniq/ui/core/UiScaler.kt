package com.example.memotrip_kroniq.ui.core

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class UiScaler(
    val scaleX: Float,
    val scaleY: Float
)

val LocalUiScaler = staticCompositionLocalOf<UiScaler> {
    error("UiScaler not provided")
}

// helpers
fun Float.sx(s: UiScaler) = (this * s.scaleX).dp
fun Float.sy(s: UiScaler) = (this * s.scaleY).dp
fun Float.fs(s: UiScaler) = (this * ((s.scaleX + s.scaleY) / 2f)).sp
