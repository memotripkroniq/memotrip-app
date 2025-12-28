package com.example.memotrip_kroniq.ui.addtrip.components

import PreviewUiScaler
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrip_kroniq.ui.addtrip.TransportType
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun TransportSelector(
    selected: Set<TransportType>,
    onSelectionChange: (Set<TransportType>) -> Unit,
    error: Boolean,
    showError: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val errorGreen = Color(0xFF759F67)
        Text(
            text = "Transport",
            color =
                if (error && showError) errorGreen
                else Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )


        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TransportType.values()) { transport ->

                val isSelected = selected.contains(transport)

                TransportItem(
                    transport = transport,
                    selected = isSelected,
                    onClick = {
                        val newSelection = when {
                            isSelected ->
                                selected - transport          // odklik

                            selected.size < 2 ->
                                selected + transport          // přidání

                            else ->
                                selected                      // ❌ třetí nejde
                        }

                        onSelectionChange(newSelection)
                    }
                )
            }
        }
    }
}

@Composable
private fun TransportItem(
    transport: TransportType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = Color(0xFF747781)
    val iconColor = if (selected) Color.White else Color(0xFF383A41)

    Box(
        modifier = Modifier
            .size(80.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {

        // 🔹 INSIDE BORDER – STEJNÉ JAKO DESTINATION
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 3.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = transport.iconRes),
                contentDescription = transport.name,
                modifier = Modifier.size(80.dp),
                colorFilter = ColorFilter.tint(iconColor)
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun TransportSelectorPreview_Selected() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            TransportSelector(
                selected = setOf(TransportType.CAR, TransportType.CARAVAN),
                onSelectionChange = {},
                error = true,
                showError = true
            )
        }
    }
}



