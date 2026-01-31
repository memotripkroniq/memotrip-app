package com.example.memotrip_kroniq.ui.home.components.upsell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun OutlinedTitleText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    strokeWidth: Float = 3f,
    strokeColor: Color = Color(0x33000000),
    fillColor: Color = Color.White
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = strokeColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                drawStyle = Stroke(width = strokeWidth)
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )

        Text(
            text = text,
            color = fillColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )
    }
}