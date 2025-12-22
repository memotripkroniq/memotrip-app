package com.example.memotrip_kroniq.ui.home.components.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

fun Modifier.innerTopShadow(
    alpha: Float = 0.45f,
    height: Float = 20f
): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()

        val shadowBrush = Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(alpha = alpha),
                Color.Transparent
            ),
            startY = 0f,
            endY = height
        )

        drawRect(
            brush = shadowBrush,
            topLeft = Offset(0f, 0f),
            size = Size(size.width, height),
            blendMode = BlendMode.Multiply
        )
    }
)

fun Modifier.innerShadow(
    color: Color = Color.White.copy(alpha = 1f), // #FFFFFF 100 %
    offsetX: Float = 0f,
    offsetY: Float = 1f,
    blur: Float = 2f,
    cornerRadius: Float = 10f
): Modifier {

    return this.drawBehind {
        drawIntoCanvas { canvas ->

            val paint = androidx.compose.ui.graphics.Paint().also {
                it.color = color
                it.asFrameworkPaint().maskFilter =
                    android.graphics.BlurMaskFilter(blur, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }

            // Horní inner shadow (posun Y)
            canvas.drawRoundRect(
                left = 0f,
                top = -blur + offsetY,
                right = size.width,
                bottom = offsetY,
                radiusX = cornerRadius,
                radiusY = cornerRadius,
                paint = paint
            )
        }
    }
}



