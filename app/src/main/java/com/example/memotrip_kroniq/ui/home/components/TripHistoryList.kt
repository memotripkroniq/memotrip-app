package com.example.memotrip_kroniq.ui.home.components

import PreviewUiScaler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.home.components.upsell.PlansTable
import com.example.memotrip_kroniq.ui.home.model.TripHistoryItem
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.home.components.upsell.UpsellPromptBox

@Composable
fun TripHistoryList(
    trips: List<TripHistoryItem>,
    showUpsell: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(trips) { trip ->
            TripHistoryItemRow(
                item = trip,
                onClick = {
                    // navController.navigate("tripDetail/${trip.id}")
                }
            )
        }

        if (showUpsell) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                UpsellPromptBox()
                Spacer(modifier = Modifier.height(12.dp))
                PlansTable(
                    onGetPremiumClick = { /* TODO */ },
                    onGetKroniqClick = { /* TODO */ }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}




@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun TripHistoryListPreview_UpsellOn() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                TripHistoryList(
                    trips = listOf(
                        TripHistoryItem(
                            id = "1",
                            title = "TestsIsPremium",
                            coverImageUrl = null
                        )
                    ),
                    showUpsell = true
                )
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun TripHistoryListPreview_UpsellOff() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                TripHistoryList(
                    trips = listOf(
                        TripHistoryItem(
                            id = "1",
                            title = "KroniQ user (no upsell)",
                            coverImageUrl = null
                        )
                    ),
                    showUpsell = false
                )
            }
        }
    }
}