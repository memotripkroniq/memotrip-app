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
import com.example.memotrip_kroniq.data.payments.DummyPaymentPlan
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.home.HomePlanTier
import com.example.memotrip_kroniq.ui.home.components.upsell.PlansTable
import com.example.memotrip_kroniq.ui.home.model.TripHistoryItem
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.home.components.upsell.UpsellPromptBox

@Composable
fun TripHistoryList(
    trips: List<TripHistoryItem>,
    showUpsell: Boolean,
    showUpsellPrompt: Boolean,
    currentPlan: HomePlanTier,
    onTripClick: (String) -> Unit,
    onGetPremiumClick: () -> Unit,
    onGetKroniqClick: () -> Unit,
    dummyPayLoadingPlan: DummyPaymentPlan?

) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = trips,
            key = { trip -> trip.id }
        ) { trip ->
            TripHistoryItemRow(
                item = trip,
                onClick = { onTripClick(trip.id) }
            )
        }

        if (showUpsell) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                if (showUpsellPrompt) {
                    UpsellPromptBox()
                    Spacer(modifier = Modifier.height(12.dp))
                }
                PlansTable(
                    currentPlan = currentPlan,
                    onGetPremiumClick = onGetPremiumClick,
                    onGetKroniqClick = onGetKroniqClick,
                    premiumEnabled = dummyPayLoadingPlan == null,
                    kroniqEnabled = dummyPayLoadingPlan == null
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
                            coverImageUrl = null,
                            theme = null,
                            isSharedInKroniq = true
                        )
                    ),
                    showUpsell = true,
                    showUpsellPrompt = true,
                    currentPlan = HomePlanTier.FREE,
                    onTripClick = {},
                    onGetPremiumClick = {},
                    onGetKroniqClick = {},
                    dummyPayLoadingPlan = null
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
                            coverImageUrl = null,
                            theme = null,
                            isSharedInKroniq = false
                        )
                    ),
                    showUpsell = false,
                    showUpsellPrompt = false,
                    currentPlan = HomePlanTier.KRONIQ,
                    onTripClick = {},
                    onGetPremiumClick = {},
                    onGetKroniqClick = {},
                    dummyPayLoadingPlan = null
                )
            }
        }
    }
}
