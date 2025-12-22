package com.example.memotrip_kroniq.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.*

@UnstableApi
@Composable
fun HeroBanner(
    onAddTripClick: () -> Unit
) {
    val context = LocalContext.current

    val isInPreview = LocalInspectionMode.current

    val s = LocalUiScaler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101318))
    ) {
        Box(Modifier.fillMaxSize()) {

            if (!isInPreview) {
                VideoBackground()
            } else {
                // Fallback obrázek pro Preview
                Image(
                    painter = painterResource(R.drawable.homescreen_planet),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // // 🔥 KLIKATELNÉ LOGO
            Image(
                painter = painterResource(R.drawable.homescreen_ic_add_trip_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(72f.sx(s))
                    .align(Alignment.Center)
                    .offset(y = (-55).dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onAddTripClick()
                    }
            )
        }
    }
}


