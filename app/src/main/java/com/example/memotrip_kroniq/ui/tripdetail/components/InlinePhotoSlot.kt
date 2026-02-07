package com.example.memotrip_kroniq.ui.tripdetail.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.memotrip_kroniq.R

@Composable
fun InlinePhotoSlot(
    imageUri: Uri?,
    onAddPhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (imageUri != null) {
        AsyncImage(
            model = imageUri,
            contentDescription = null,
            modifier = modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAddPhotoClick() },
            contentScale = ContentScale.Crop
        )
    } else {
        Icon(
            painter = painterResource(R.drawable.add_trip_name_field),
            contentDescription = null,
            tint = Color(0xFF747781).copy(alpha = 0.8f),
            modifier = modifier
                .size(38.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAddPhotoClick() }
        )
    }
}
