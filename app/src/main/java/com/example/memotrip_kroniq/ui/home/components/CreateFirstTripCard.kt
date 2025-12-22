package com.example.memotrip_kroniq.ui.home.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrip_kroniq.R


@Composable
fun CreateFirstTripCard(
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(
                color = Color(0xFF383A41),
                shape = RoundedCornerShape(8.dp)
            )
            .then(
                if (onClick != null) {
                    Modifier
                        .padding(horizontal = 12.dp)
                } else Modifier
            )
    ) {

        // 🔹 LEVÁ IKONA
        Icon(
            painter = painterResource(id = R.drawable.ic_add_first_trip), // 👈 tvoje ikona s pluskem
            contentDescription = "Add trip",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(44.dp)
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
        )

        // 🔹 CENTROVANÝ TEXT
        Text(
            text = "Create your first Trip !",
            color = Color(0xFF759F67),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        )
    }
}

