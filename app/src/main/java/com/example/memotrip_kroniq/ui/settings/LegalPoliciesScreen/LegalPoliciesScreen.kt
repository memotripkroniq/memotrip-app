package com.example.memotrip_kroniq.ui.settings

import PreviewUiScaler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.navigation.Screen
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.settings.components.SettingsArrowItem
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun LegalPoliciesScreen(
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AppTopBar(
            title = stringResource(R.string.settings_legal),
            showBack = true,
            onBackClick = { navController.popBackStack() },
            onMenuClick = null
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsArrowItem(
                title = stringResource(R.string.legal_privacy_policy),
                onClick = { navController.navigate(Screen.PrivacyPolicy.route) }
            )

            SettingsArrowItem(
                title = stringResource(R.string.legal_terms_of_service),
                onClick = { navController.navigate(Screen.TermsOfService.route) }
            )

            SettingsArrowItem(
                title = stringResource(R.string.legal_payments_and_subscriptions),
                onClick = { navController.navigate(Screen.PaymentsSubscriptions.route) }
            )

            SettingsArrowItem(
                title = stringResource(R.string.legal_ai_features_notice),
                onClick = { navController.navigate(Screen.AiFeaturesNotice.route) }
            )

            SettingsArrowItem(
                title = stringResource(R.string.legal_open_source_licenses),
                onClick = { navController.navigate(Screen.OpenSourceLicenses.route) }
            )

            SettingsArrowItem(
                title = stringResource(R.string.legal_delete_account),
                onClick = { navController.navigate(Screen.DeleteAccount.route) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun LegalPoliciesScreenPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            LegalPoliciesScreen(
                navController = rememberNavController()
            )
        }
    }
}
