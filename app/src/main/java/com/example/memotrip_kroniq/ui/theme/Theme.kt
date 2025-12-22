package com.example.memotrip_kroniq.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MemoTripColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = TextWhite,
    background = BlackBackground,
    surface = SurfaceDark,
    onSurface = TextWhite
)

@Composable
fun MemoTripTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MemoTripColorScheme,
        typography = MemoTripTypography,
        content = content
    )
}
