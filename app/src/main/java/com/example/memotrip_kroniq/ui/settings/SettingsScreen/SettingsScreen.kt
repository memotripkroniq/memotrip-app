package com.example.memotrip_kroniq.ui.settings

import PreviewUiScaler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.navigation.Screen
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
    val context = LocalContext.current

    var pushNotifications by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(true) }
    var isKroniq by remember { mutableStateOf(false) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var kroniqImageUrl by remember { mutableStateOf<String?>(null) }
    var isSettingsLoading by remember { mutableStateOf(true) }
    val tokenStore = remember { TokenDataStore(context) }
    val authRepository = remember {
        AuthRepository(
            api = RetrofitClient.authApi,
            tokenStore = tokenStore
        )
    }

    LaunchedEffect(authRepository) {
        runCatching { authRepository.getMe() }
            .onSuccess { me ->
                isKroniq = me.isKroniq
                profileImageUrl = me.profileImageUrl
                kroniqImageUrl = me.kroniqImageUrl
                isSettingsLoading = false
            }
            .onFailure {
                isKroniq = false
                profileImageUrl = null
                kroniqImageUrl = null
                isSettingsLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        AppTopBar(
            title = stringResource(R.string.settings_title),
            showBack = true,
            onBackClick = { navController.popBackStack() },
            onMenuClick = null
        )

        Crossfade(
            targetState = isSettingsLoading,
            modifier = Modifier.fillMaxSize(),
            label = "settings_loading"
        ) { loading ->
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsArrowItem(
                        title = stringResource(R.string.settings_profile),
                        leadingImageModel = profileImageUrl,
                        onClick = {
                            navController.navigate(Screen.Profile.route)
                        }
                    )

                    SettingsArrowItem(
                        title = stringResource(R.string.profile_account_type_kroniq),
                        enabled = isKroniq,
                        trailingIconRes = if (isKroniq) null else R.drawable.homescreen_ic_lock_theme,
                        leadingImageModel = kroniqImageUrl,
                        onClick = { navController.navigate(Screen.KroniQ.route) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsArrowItem(
                        title = stringResource(R.string.settings_location),
                        onClick = { /* TODO */ }
                    )

                    SettingsArrowItem(
                        title = stringResource(R.string.settings_language),
                        onClick = { navController.navigate(Screen.Language.route) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SectionTitle(stringResource(R.string.settings_notifications))

                    SettingsSwitchItem(
                        title = stringResource(R.string.settings_push_notifications),
                        checked = pushNotifications,
                        onCheckedChange = { pushNotifications = it }
                    )

                    SettingsSwitchItem(
                        title = stringResource(R.string.settings_email_notifications),
                        checked = emailNotifications,
                        onCheckedChange = { emailNotifications = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SectionTitle(stringResource(R.string.settings_supports))

                    SettingsArrowItem(
                        title = stringResource(R.string.settings_change_password),
                        onClick = { navController.navigate(Screen.ChangePassword.route) }
                    )

                    SettingsArrowItem(
                        title = stringResource(R.string.settings_legal),
                        onClick = { /* TODO */ }
                    )

                    SettingsArrowItem(
                        title = stringResource(R.string.settings_help),
                        onClick = { /* TODO */ }
                    )

                    SettingsArrowItem(
                        title = stringResource(R.string.settings_rate_us),
                        onClick = { /* TODO */ }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LogoutButton(
                        text = stringResource(R.string.settings_log_out),
                        onClick = onLogoutClick
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
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
