package com.example.memotrip_kroniq.ui.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.UiScaler

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    BoxWithConstraints {

        val baselineWidth = 412f
        val baselineHeight = 892f

        val scaleX = maxWidth.value / baselineWidth
        val scaleY = maxHeight.value / baselineHeight

        val scaler = UiScaler(scaleX, scaleY)

        CompositionLocalProvider(
            LocalUiScaler provides scaler
        ) {
            content()
        }
    }
}
