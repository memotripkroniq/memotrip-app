package com.example.memotrip_kroniq.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.material3.Icon
import androidx.compose.runtime.remember
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.home.model.TripHistoryItem

@Composable
fun TripHistoryItemRow(
    item: TripHistoryItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF383A41))
            .clickable(
                enabled = onClick != null,
                interactionSource = interactionSource,
                indication = null // 🚫 ripple pryč
            ) {
                onClick?.invoke()
            }
            .padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 🔹 LEVÝ OBRÁZEK (jen pokud existuje)
        if (item.coverImageUrl != null) {
            AsyncImage(
                model = item.coverImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(39.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))
        }

        // 🔹 NÁZEV TRIPU
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f),
            maxLines = 1
        )

        // 🔹 ZELENÁ ŠIPKA – DETAIL TRIPU
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = "Open trip detail",
            tint = Color(0xFF759F67),
            modifier = Modifier
                .size(20.dp)
        )
    }
}