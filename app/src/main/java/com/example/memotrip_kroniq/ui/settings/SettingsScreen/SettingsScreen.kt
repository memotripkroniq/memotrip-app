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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.settings.components.LogoutButton
import com.example.memotrip_kroniq.ui.settings.components.SectionTitle
import com.example.memotrip_kroniq.ui.settings.components.SettingsArrowItem
import com.example.memotrip_kroniq.ui.settings.components.SettingsSwitchItem
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

private val ScreenBg = Color.Black

@Composable
fun SettingsScreen(
    navController: NavHostController,
    onLogoutClick: () -> Unit
) {
    val s = LocalUiScaler.current

    var pushNotifications by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        AppTopBar(
            title = "Settings",
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
                title = "Profile",
                onClick = { /* TODO */ }
            )

            SettingsArrowItem(
                title = "KroniQ 🔒",
                onClick = { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsArrowItem(
                title = "Location",
                onClick = { /* TODO */ }
            )

            SettingsArrowItem(
                title = "Language",
                onClick = { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SectionTitle("Notifications")

            SettingsSwitchItem(
                title = "Push notifications",
                checked = pushNotifications,
                onCheckedChange = { pushNotifications = it }
            )

            SettingsSwitchItem(
                title = "Email notifications",
                checked = emailNotifications,
                onCheckedChange = { emailNotifications = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SectionTitle("Supports")

            SettingsArrowItem(
                title = "Change password",
                onClick = { /* TODO */ }
            )

            SettingsArrowItem(
                title = "Legal & Policies",
                onClick = { /* TODO */ }
            )

            SettingsArrowItem(
                title = "Help & Support",
                onClick = { /* TODO */ }
            )

            SettingsArrowItem(
                title = "Rate us",
                onClick = { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LogoutButton(
                text = "Log out",
                onClick = onLogoutClick
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Preview(showBackground = true, widthDp = 412, heightDp = 950)
@Composable
fun SettingsScreenPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            SettingsScreen(
                navController = rememberNavController(),
                onLogoutClick = {}
            )
        }
    }
}