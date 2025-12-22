package com.example.memotrip_kroniq.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TripHistoryEmptyContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        CreateFirstTripCard()

        Spacer(modifier = Modifier.height(96.dp))

        TripVideoPlaceholder()
    }
}
