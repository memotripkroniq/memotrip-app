package com.example.memotrip_kroniq.ui.addtrip.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun LocationField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: Boolean
) {
    val errorGreen = Color(0xFF759F67)
    val isEmpty = value.isBlank()

    Column {

        Text(
            text = label,
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
                .background(
                    color = Color(0xFF383A41),
                    shape = RoundedCornerShape(10.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = if (error) errorGreen else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                )

                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.fillMaxWidth()
            )

            if (isEmpty) {
                Text(
                    text = "Add ${label.lowercase()} destination",
                    color =
                        if (error) errorGreen
                        else Color.Gray,
                    fontSize = 16.sp
                )
            }

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
                onValueChange = {},
                error = true
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
            onValueChange = {},
            error = true
        )
    }
}
