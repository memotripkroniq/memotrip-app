package com.example.memotrip_kroniq.ui.addtrip.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

// 🟢 prostor pro křížek
private val RemoveIconSpace = 36.dp

@Composable
fun WaypointField(
    index: Int,
    value: String,
    error: Boolean,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val errorGreen = Color(0xFF759F67)
    val isEmpty = value.isBlank()

    Column {

        Text(
            text = stringResource(R.string.add_trip_stop_number, index + 1),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF383A41))
                .border(
                    width = 1.5.dp,
                    color = if (error) errorGreen else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.CenterStart
        ) {

            Text(
                text = if (isEmpty) stringResource(R.string.add_trip_stop_placeholder) else value,
                color = if (isEmpty) {
                    if (error) errorGreen else Color.Gray
                } else {
                    Color.White
                },
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = RemoveIconSpace
                )
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_wp_close),
                contentDescription = stringResource(R.string.add_trip_remove_stop),
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(15.dp)
                    .clickable { onRemoveClick() }
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WaypointFieldPreview() {
    MemoTripTheme {
        Column(Modifier.padding(24.dp)) {
            WaypointField(
                index = 0,
                value = "Vienna, Austria",
                error = false,
                onClick = {},
                onRemoveClick = {}
            )

            Spacer(Modifier.height(16.dp))

            WaypointField(
                index = 1,
                value = "",
                error = true,
                onClick = {},
                onRemoveClick = {}
            )
        }
    }
}
