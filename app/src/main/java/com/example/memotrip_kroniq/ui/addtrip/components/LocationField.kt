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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun LocationField(
    label: String,
    value: String,
    error: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bottomSpacing: Dp = 0.dp
) {
    val errorGreen = Color(0xFF759F67)
    val isEmpty = value.isBlank()

    Column(
        modifier = modifier.padding(bottom = bottomSpacing)
    ) {

        // LABEL – NENÍ KLIKATELNÝ
        Text(
            text = label,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // INPUT BOX – JEDINÉ KLIKATELNÉ MÍSTO
        Row(
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
                .clickable(onClick = onClick)   // ✅ jen input
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = if (isEmpty) "Add ${label.lowercase()} destination" else value,
                color = when {
                    isEmpty && error -> errorGreen
                    isEmpty -> Color.Gray
                    else -> Color.White
                },
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = errorGreen
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun LocationFieldEmptyPreview() {
    MemoTripTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1F24))
                .padding(24.dp)
        ) {
            LocationField(
                label = "From",
                value = "",
                error = true,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun LocationFieldFilledPreview() {
    MemoTripTheme {
        LocationField(
            label = "To",
            value = "Rome, Italy",
            error = false,
            onClick = {}
        )
    }
}
