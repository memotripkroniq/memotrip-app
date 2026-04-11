package com.example.memotrip_kroniq.ui.addtrip.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.components.PrimaryAiButton
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun AddTripHeroBanner(
    imageUrl: String?,
    isGenerating: Boolean,
    isMapDirty: Boolean,
    error: Boolean,
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    // ⭐ ZMĚNA 1: URL, KTERÁ SE OPRAVDU ZOBRAZUJE
    var displayedImageUrl by remember {
        mutableStateOf<String?>(null)
    }

    // ⭐ ZMĚNA 2: aktualizujeme AŽ KDYŽ PŘIJDE NOVÁ MAPA
    LaunchedEffect(imageUrl) {
        if (imageUrl != null) {
            displayedImageUrl = imageUrl
        }
    }

    // ⭐ ZMĚNA 3: painter má STABILNÍ model
    val painter = rememberAsyncImagePainter(
        model = displayedImageUrl ?: R.drawable.add_trip_screen_maps_banner
    )

    val errorGreen = Color(0xFF759F67)
    val borderColor = if (error) errorGreen else Color.Transparent


    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {

        // 🗺️ MAPA – BITMAPA SE NIKDY NEZTRATÍ
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
        ) {

            Image(
                painter = painter,
                contentDescription = stringResource(R.string.add_trip_trip_map),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 🅰️ PRVNÍ GENEROVÁNÍ
            if (displayedImageUrl == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
                    ) {
                        PrimaryAiButton(
                            text = if (isGenerating) {
                                stringResource(R.string.add_trip_generating)
                            } else {
                                stringResource(R.string.add_trip_generate)
                            },
                            isLoading = isGenerating,
                            onClick = {
                                if (!isGenerating) {
                                    onGenerateClick()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.48f)
                                .height(45.dp)
                        )
                    }
                }
            }
        }

        // 🔁 REGENERATE IKONA – BEZ PŘEKRYTÍ MAPY
        if (displayedImageUrl != null && (isMapDirty || isGenerating)) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 12.dp, y = (-12).dp)
                    .size(42.dp)
                    .zIndex(10f)
                    .clickable(
                        enabled = !isGenerating,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onGenerateClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_ai_refresh),
                        contentDescription = stringResource(R.string.add_trip_regenerate_map),
                        modifier = Modifier.size(35.dp)
                    )
                }
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
            isMapDirty = false,
            onGenerateClick = {},
            error = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "Map + Regenerate Icon",
    showBackground = true,
    widthDp = 412,
    heightDp = 300
)
@Composable
private fun AddTripHeroBannerPreview_Regenerate() {
    MemoTripTheme {
        AddTripHeroBanner(
            imageUrl = "https://fake.map.url/map.png",
            isGenerating = false,
            isMapDirty = true,
            onGenerateClick = {},
            error = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}
