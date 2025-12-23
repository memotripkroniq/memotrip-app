package com.example.memotrip_kroniq.ui.addtrip.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
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
    startDate: LocalDate?,
    endDate: LocalDate?,
    showError: Boolean,
    onClick: () -> Unit
) {
    val text = when {
        startDate != null && endDate != null -> {
            val start = startDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            val end = endDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            "$start – $end"
        }
        else -> "Add trip dates"
    }


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
                .background(
                    color = Color(0xFF383A41),
                    shape = RoundedCornerShape(10.dp)
                )
                .border(
                    BorderStroke(1.dp, Color(0xFF2B2E34)),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = text,
                color = if (startDate == null || endDate == null)
                    Color.Gray
                else
                    Color.White,
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

        if (showError) {
            Text(
                text = "Date is required",
                color = Color(0xFFE06B6B),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
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
            startDate = null,
            endDate = null,
            showError = false, // 👈 DOPLNĚNO
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
            startDate = LocalDate.of(2025, 6, 28),
            endDate = LocalDate.of(2025, 7, 11),
            showError = false, // 👈 DOPLNĚNO
            onClick = {}
        )
    }
}

