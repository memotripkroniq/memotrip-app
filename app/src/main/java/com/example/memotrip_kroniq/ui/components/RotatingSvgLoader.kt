package com.example.memotrip_kroniq.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R

@Composable
fun RotatingSvgLoader(
    modifier: Modifier = Modifier,
    durationMs: Int = 2000
) {
    val transition = rememberInfiniteTransition(label = "loader")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // 🔑 VĚTŠÍ KONTEJNER
    Box(
        modifier = modifier
            .size(156.dp) // ⬅️ VĚDOMĚ VĚTŠÍ NEŽ IMAGE
            .graphicsLayer {
                rotationZ = rotation
                transformOrigin = TransformOrigin.Center
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.loader),
            contentDescription = null,
            modifier = Modifier.size(142.dp), // ⬅️ SKUTEČNÁ VELIKOST LOADERU
            contentScale = ContentScale.Fit
        )
    }
}