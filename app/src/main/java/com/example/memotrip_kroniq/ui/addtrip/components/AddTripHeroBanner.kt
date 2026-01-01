package com.example.memotrip_kroniq.ui.addtrip.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.components.PrimaryAiButton
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import android.util.Log


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddTripHeroBanner(
    imageUrl: String?,
    isGenerating: Boolean,
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E1E))
    ) {

        // 🗺️ Mapa / default obrázek
        Crossfade(
            targetState = imageUrl,
            label = "TripBannerCrossfade"
        ) { url ->
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = "Generated trip map",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.add_trip_screen_maps_banner),
                    contentDescription = "Default trip hero banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // ✨ Generate overlay (jen pokud mapa ještě není)
        if (imageUrl == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                PrimaryAiButton(
                    text = if (isGenerating) "Generating…" else "Generate",
                    isLoading = isGenerating,
                    onClick = {
                        Log.d("AddTripHero", "✅ Generate clicked")
                        onGenerateClick()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 300)
@Composable
private fun AddTripHeroBannerPreview() {
    MemoTripTheme {
        AddTripHeroBanner(
            imageUrl = null,
            isGenerating = false,
            onGenerateClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
