package com.example.memotrip_kroniq.ui.addtrip.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.addtrip.Destination
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import androidx.compose.ui.unit.sp


@Composable
fun DestinationSelector(
    selected: Destination?,
    onSelect: (Destination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Text(
            text = "Destination",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            items(Destination.values()) { destination ->
                DestinationItem(
                    destination = destination,
                    selected = destination == selected,
                    onClick = { onSelect(destination) }
                )
            }
        }
    }
}


@Composable
private fun DestinationItem(
    destination: Destination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = Color(0xFF747781)
    val iconColor = if (selected) Color.White else Color(0xFF383A41)

    Box(
        modifier = Modifier
            .size(80.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {

        // 🔹 INSIDE BORDER SIMULACE
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 3.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(6.dp), // 👈 simuluje inside stroke
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(id = destination.iconRes),
                contentDescription = destination.name,
                modifier = Modifier.size(80.dp),
                colorFilter = ColorFilter.tint(iconColor)
            )
        }
    }
}


