package com.example.memotrip_kroniq.ui.tripdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import coil.size.Size


@Composable
fun ZoomableImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {

        // 🔹 celý screen – klik mimo mapu = zavřít
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {

            // 🗺️ MAPA – zastaví kliky, aby se nezavřela
            Box(
                modifier = Modifier
                    .fillMaxSize(0.9f)
                    .aspectRatio(1.6f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .clickable( // ← důležité: “sežere” klik
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { }
            ) {

                ZoomableAsyncImage(
                    imageUrl = imageUrl,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ZoomableAsyncImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val context = LocalContext.current

    val state = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        if (newScale == 1f) offset = Offset.Zero else offset += panChange
        scale = newScale
    }

    Box(modifier = modifier.transformable(state)) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .size(Size.ORIGINAL) // ✅ nedegraduje na “velikost view”
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}
