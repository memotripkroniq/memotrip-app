package com.example.memotrip_kroniq.ui.tripdetail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.model.ThemeType
import com.example.memotrip_kroniq.ui.core.model.TransportType
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerTopShadow
import com.example.memotrip_kroniq.ui.theme.AppTheme

@Composable
fun TripInfoCard(
    dateText: String,
    fromText: String,
    toText: String,
    transport: Set<TransportType>,
    onEditClick: () -> Unit,
    theme: ThemeType?
) {
    val ui = LocalUiScaler.current
    val cardColor = Color(0xFF383A41)
    val radius = 12.dp

    Column {
        Text(
            text = stringResource(R.string.trip_detail_trip_info),
            color = Color.White,
            fontSize = 16f.fs(ui),
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10f.sy(ui)))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(radius))
                .background(cardColor, RoundedCornerShape(radius))
                .innerTopShadow(alpha = 0.18f, height = 18f)
                .padding(14.dp)
        ) {

            Box(modifier = Modifier.fillMaxWidth()) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 58.dp)
                ) {
                    Text(
                        stringResource(R.string.trip_detail_date),
                        color = Color.White,
                        fontSize = 16f.fs(ui),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6f.sy(ui)))
                    Text(dateText, color = Color.White, fontSize = 16f.fs(ui))

                    Spacer(Modifier.height(14f.sy(ui)))

                    Text(
                        stringResource(R.string.trip_detail_location),
                        color = Color.White,
                        fontSize = 16f.fs(ui),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6f.sy(ui)))
                    Text(
                        text = stringResource(R.string.trip_detail_from, fromText),
                        color = Color.White,
                        fontSize = 16f.fs(ui),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4f.sy(ui)))
                    Text(
                        text = stringResource(R.string.trip_detail_to, toText),
                        color = Color.White,
                        fontSize = 16f.fs(ui),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(14f.sy(ui)))

                    Text(
                        stringResource(R.string.trip_detail_transport),
                        color = Color.White,
                        fontSize = 16f.fs(ui),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6f.sy(ui)))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        transport.forEach { t -> TripInfoTransportItem(transport = t) }
                    }
                }

                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    ThemePreview(theme = theme)
                }
            }

            Image(
                painter = painterResource(R.drawable.tripdetail_ic_edit),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onEditClick() }
            )
        }
    }
}

@Composable
private fun ThemePreview(theme: ThemeType?) {
    if (theme != null) {
        ThemePreviewImage(resId = theme.imageRes) // ✅ používáme to co už máš v enumu
    } else {
        ThemePreviewPlaceholder() // Theme + lock
    }
}

@Composable
private fun ThemePreviewImage(resId: Int) {
    val borderColor = Color(0xFF747781)

    Box(
        modifier = Modifier
            .size(80.dp)
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = stringResource(R.string.trip_detail_theme_preview),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ThemePreviewPlaceholder() {
    val ui = LocalUiScaler.current
    val borderColor = Color(0xFF747781)

    Box(
        modifier = Modifier
            .size(80.dp)
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF383A41), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.trip_detail_theme),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 16f.fs(ui),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Image(
                painter = painterResource(R.drawable.homescreen_ic_lock_theme),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TripInfoTransportItem(transport: TransportType) {
    val borderColor = Color(0xFF747781)

    Box(
        modifier = Modifier
            .size(56.dp)
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = transport.iconRes),
            contentDescription = transport.name,
            modifier = Modifier.size(80.dp),
            colorFilter = ColorFilter.tint(Color.White)
        )
    }
}

@Preview(
    name = "TripInfoCard – LOCKED",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 340
)
@Composable
private fun TripInfoCardLockedPreview() {
    AppTheme {
        TripInfoCard(
            dateText = "28 June 2025 - 11 July 2025",
            fromText = "Slovenský Grob, Slovakia",
            toText = "Camping Lacona Pineta, Elba",
            transport = setOf(TransportType.PLANE, TransportType.CAR),
            onEditClick = {},
            theme = null
        )
    }
}

@Preview(
    name = "TripInfoCard – SUMMER",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 340
)
@Composable
private fun TripInfoCardSummerPreview() {
    AppTheme {
        TripInfoCard(
            dateText = "28 June 2025 - 11 July 2025",
            fromText = "Slovenský Grob, Slovakia",
            toText = "Camping Lacona Pineta, Elba",
            transport = setOf(TransportType.PLANE, TransportType.CAR),
            onEditClick = {},
            theme = ThemeType.SUMMER
        )
    }
}
