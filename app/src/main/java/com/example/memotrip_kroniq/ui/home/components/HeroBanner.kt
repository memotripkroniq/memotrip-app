package com.example.memotrip_kroniq.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@UnstableApi
@Composable
fun HeroBanner(
    onAddTripClick: () -> Unit,
    isAddTripEnabled: Boolean
) {

    val isInPreview = LocalInspectionMode.current

    val s = LocalUiScaler.current

    var isVideoReady by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101318))
    ) {
        Box(Modifier.fillMaxSize()) {

            // ✅ fallback obrázek je vždy dole
            Image(
                painter = painterResource(R.drawable.homescreen_planet),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // ✅ video vykresli jen mimo preview
            if (!isInPreview) {
                VideoBackground(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (isVideoReady) 1f else 0f),
                    onReady = {
                        isVideoReady = true
                    }
                )
            }

            // 🔥 KLIKATELNÉ LOGO
            Image(
                painter = painterResource(R.drawable.homescreen_ic_add_trip_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(72f.sx(s))
                    .align(Alignment.Center)
                    .offset(y = (-55).dp)
                    .alpha(if (isAddTripEnabled) 1f else 0.38f)
                    .clickable(
                        enabled = isAddTripEnabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onAddTripClick()
                    }
            )
        }
    }
}


