package com.example.memotrip_kroniq.ui.addtrip.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memotrip_kroniq.R

@Composable
fun TripSuccessContent(
    title: String = stringResource(R.string.add_trip_success_title),
    subtitle: String = stringResource(R.string.add_trip_success_subtitle),
    footer: String = stringResource(R.string.add_trip_success_footer)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = title,
                color = Color(0xFF7FAF7A),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = subtitle,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(R.drawable.ic_trip_success),
                contentDescription = null,
                modifier = Modifier.size(140.dp)
            )

            Spacer(modifier = Modifier.height(70.dp))

            Text(
                text = footer,
                color = Color(0xFF7FAF7A),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
