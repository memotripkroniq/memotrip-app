package com.example.memotrip_kroniq.ui.tripdetail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun TripMapPreview(
    mapImageUrl: String?,
    modifier: Modifier = Modifier
) {
    var showMap by remember { mutableStateOf(false) }

    if (!mapImageUrl.isNullOrBlank()) {
        AsyncImage(
            model = mapImageUrl,
            contentDescription = "Trip map",
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { showMap = true },
            contentScale = ContentScale.Crop
        )
    }

    if (showMap && !mapImageUrl.isNullOrBlank()) {
        ZoomableImageDialog(
            imageUrl = mapImageUrl!!,
            onDismiss = { showMap = false }
        )
    }
}