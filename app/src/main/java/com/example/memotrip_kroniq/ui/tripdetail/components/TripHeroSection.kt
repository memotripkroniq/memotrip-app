package com.example.memotrip_kroniq.ui.tripdetail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.memotrip_kroniq.ui.theme.AppTheme

@Composable
fun TripHeroSection(
    modifier: Modifier = Modifier,
    coverUrl: String? = null,
    mapUrl: String? = null,
    canEdit: Boolean = true,
    onChangeCoverClick: () -> Unit = {},
    onMapClick: () -> Unit = {}
) {
    val height = 117.dp
    val radius = 14.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // COVER
        Box(modifier = Modifier.size(height)) {

            if (coverUrl != null) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(radius)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(radius))
                        .background(Color.DarkGray)
                )
            }

            if (canEdit) {
                Image(
                    painter = painterResource(R.drawable.tripdetail_ic_edit),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 12.dp, y = 12.dp)
                        .size(32.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onChangeCoverClick() }
                )
            }
        }

        Spacer(modifier = Modifier.width(18.dp))

        // MAP
        Box(
            modifier = Modifier
                .height(height)
                .weight(1f)
                .clip(RoundedCornerShape(radius))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onMapClick() }
        ) {
            if (!mapUrl.isNullOrBlank()) {
                AsyncImage(
                    model = mapUrl,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Gray)
                )
            }
        }

    }
}

@Preview(
    name = "TripHeroSection",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 425
)
@Composable
private fun TripHeroSectionPreview() {
    AppTheme {
        TripHeroSection(
            coverUrl = null
        )
    }
}
