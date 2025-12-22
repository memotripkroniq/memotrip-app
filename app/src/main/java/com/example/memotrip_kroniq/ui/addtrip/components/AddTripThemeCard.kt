package com.example.memotrip_kroniq.ui.addtrip.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R


@Composable
fun AddTripThemeCard(
    modifier: Modifier = Modifier,
    imageRes: Int,
    locked: Boolean,
    selected: Boolean
) {
    val borderWidth = 3.dp
    val outerRadius = 10.dp
    val innerRadius = outerRadius - borderWidth
    val borderColor = Color(0xFF747781)

    Box(
        modifier = modifier
            .size(80.dp)
            .then(
                if (!locked && selected) {
                    Modifier
                        .border(
                            width = borderWidth,
                            color = borderColor,
                            shape = RoundedCornerShape(outerRadius)
                        )
                        .padding(borderWidth)
                } else Modifier
            )
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(innerRadius)), // 🔥 FIX
            contentScale = ContentScale.Crop,
            alpha = if (locked) 0.8f else 1f
        )

        if (locked) {
            Image(
                painter = painterResource(R.drawable.homescreen_ic_lock_theme),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(16.dp)
                    .offset(y = 15.dp)
            )
        }
    }
}