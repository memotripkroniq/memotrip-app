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
import androidx.compose.ui.draw.clip
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
    error: Boolean,
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

    val errorGreen = Color(0xFF759F67)



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
                .height(45.dp)
                .clickable { onClick() }
                .clip(RoundedCornerShape(10.dp))
                .background(
                    color = Color(0xFF383A41),
                    shape = RoundedCornerShape(10.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = if (error && showError) errorGreen else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                )

                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val isEmpty = startDate == null || endDate == null

            Text(
                text = text,
                color =
                    if (error && showError) errorGreen
                    else if (isEmpty) Color.Gray
                    else Color.White,
                fontSize = 16.sp,
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
            startDate = null,
            endDate = null,
            error = true,
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
            error = true,
            showError = false, // 👈 DOPLNĚNO
            onClick = {}
        )
    }
}

