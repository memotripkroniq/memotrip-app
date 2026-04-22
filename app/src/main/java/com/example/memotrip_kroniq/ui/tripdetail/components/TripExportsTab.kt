package com.example.memotrip_kroniq.ui.tripdetail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.ui.components.PrimaryAiButton
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sy
import com.memotrip_kroniq.R

@Composable
fun TripExportsTab(
    modifier: Modifier = Modifier,
    onGenerateHighlightsClick: () -> Unit = {},
    onGenerateMemoriesClick: () -> Unit = {}
) {
    val s = LocalUiScaler.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ExportSection(
            title = stringResource(R.string.trip_detail_exports_highlights),
            imageRes = R.drawable.add_trip_screen_maps_banner,
            onGenerateClick = onGenerateHighlightsClick
        )

        ExportSection(
            title = stringResource(R.string.trip_detail_exports_memories),
            imageRes = R.drawable.add_trip_screen_maps_banner,
            onGenerateClick = onGenerateMemoriesClick
        )

        Spacer(modifier = Modifier.height(6f.sy(s)))
    }
}

@Composable
private fun ExportSection(
    title: String,
    imageRes: Int,
    onGenerateClick: () -> Unit
) {
    val s = LocalUiScaler.current

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16f.fs(s),
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(182.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = title,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.38f)),
                contentAlignment = Alignment.Center
            ) {
                PrimaryAiButton(
                    text = stringResource(R.string.add_trip_generate),
                    onClick = onGenerateClick,
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .height(45.dp)
                )
            }
        }
    }
}
