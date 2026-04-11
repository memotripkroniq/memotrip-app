package com.example.memotrip_kroniq.ui.addtrip.screens

import PreviewUiScaler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.components.RotatingSvgLoader
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun SavingTripScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            RotatingSvgLoader()
            Spacer(modifier = Modifier.height(56.dp))
            Text(
                text = stringResource(R.string.add_trip_saving_experience),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

        }
    }
}

@Preview(
    name = "SavingTripScreen",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 892
)
@Composable
fun SavingTripScreenPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            SavingTripScreen()
        }
    }
}
