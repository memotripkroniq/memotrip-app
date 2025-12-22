package com.example.memotrip_kroniq.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx

@Composable
fun SocialIcon(
    icon: Int,
    sizePx: Float,
    onClick: () -> Unit
) {
    val s = LocalUiScaler.current

    Box(
        modifier = Modifier
            .size(48f.sx(s))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(sizePx.sx(s))
        )
    }
}