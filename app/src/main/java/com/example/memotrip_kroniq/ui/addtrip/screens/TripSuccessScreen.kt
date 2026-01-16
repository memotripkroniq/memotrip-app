package com.example.memotrip_kroniq.ui.addtrip.screens

import PreviewUiScaler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.example.memotrip_kroniq.navigation.Screen
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.home.HomeTab
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import kotlinx.coroutines.delay


@Composable
fun TripSuccessScreen(
    navController: NavHostController
) {
    LaunchedEffect(Unit) {
        delay(1500)
        navController.navigate(
            Screen.Home.createRoute(HomeTab.TRIP_HISTORY)
        ) {
            popUpTo(Screen.AddTrip.route) {
                inclusive = true
            }
        }

    }

    TripSuccessContent()
}


@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 892
)
@Composable
private fun TripSuccessContentPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            TripSuccessContent()
        }
    }
}




