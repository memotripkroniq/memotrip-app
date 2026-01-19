package com.example.memotrip_kroniq.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.ui.home.model.TripHistoryItem


@Composable
fun TripHistoryList(
    trips: List<TripHistoryItem>
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(trips) { trip ->
            TripHistoryItemRow(
                item = trip,
                onClick = {
                    // navController.navigate("tripDetail/${trip.id}")
                }
            )
        }
    }
}

