package com.example.memotrip_kroniq.ui.addtrip.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.memotrip_kroniq.ui.addtrip.components.DateField



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateField(
    date: LocalDate?,
    onClick: () -> Unit
) {
    val text = date?.let {
        it.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    } ?: "Add trip date"

    Column {

        // 🔹 HEADER
        Text(
            text = "Date of trip",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // 🔹 FIELD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable { onClick() }
                .border(
                    BorderStroke(1.dp, Color(0xFF2B2E34)),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = text,
                color = if (date == null) Color.Gray else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "Select date",
                tint = Color(0xFF759F67)
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun DateFieldEmptyPreview() {
    MemoTripTheme {
        DateField(
            date = null,
            onClick = {}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun DateFieldFilledPreview() {
    MemoTripTheme {
        DateField(
            date = LocalDate.now(),
            onClick = {}
        )
    }
}
