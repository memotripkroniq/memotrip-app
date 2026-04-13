package com.example.memotrip_kroniq.ui.settings

import PreviewUiScaler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

private data class OpenSourceLibrary(
    val name: String,
    val license: String,
    val usage: String
)

private val openSourceLibraries = listOf(
    OpenSourceLibrary(
        name = "AndroidX Compose / Navigation / AppCompat",
        license = "Apache License 2.0",
        usage = "Core UI, navigation, activity integration and compatibility support."
    ),
    OpenSourceLibrary(
        name = "Accompanist Navigation Animation",
        license = "Apache License 2.0",
        usage = "Animated navigation transitions."
    ),
    OpenSourceLibrary(
        name = "Media3 ExoPlayer",
        license = "Apache License 2.0",
        usage = "Video playback and media UI."
    ),
    OpenSourceLibrary(
        name = "Retrofit + Gson Converter",
        license = "Apache License 2.0",
        usage = "HTTP API communication and JSON deserialization."
    ),
    OpenSourceLibrary(
        name = "Ktor Client",
        license = "Apache License 2.0",
        usage = "Additional network communication flows."
    ),
    OpenSourceLibrary(
        name = "Coil Compose",
        license = "Apache License 2.0",
        usage = "Async image loading in Compose."
    ),
    OpenSourceLibrary(
        name = "AndroidX DataStore",
        license = "Apache License 2.0",
        usage = "Local preference and token persistence."
    ),
    OpenSourceLibrary(
        name = "Kotlin Coroutines",
        license = "Apache License 2.0",
        usage = "Asynchronous and reactive app logic."
    ),
    OpenSourceLibrary(
        name = "Firebase Auth",
        license = "Apache License 2.0",
        usage = "Authentication integration."
    ),
    OpenSourceLibrary(
        name = "Google Play Services Auth",
        license = "Google Play services terms / proprietary distribution",
        usage = "Google sign-in integration."
    )
)

@Composable
fun OpenSourceLicensesScreen(
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AppTopBar(
            title = "Open Source Licenses",
            showBack = true,
            onBackClick = { navController.popBackStack() },
            onMenuClick = null
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "This screen lists the main third-party libraries currently used by the app. If the dependency set changes, this list should be updated before release.",
                color = Color.White.copy(alpha = 0.86f)
            )

            openSourceLibraries.forEach { library ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = library.name,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = library.license,
                        color = Color(0xFF9FC780)
                    )
                    Text(
                        text = library.usage,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
            }

            Text(
                text = "This is a product-maintained summary, not a full generated OSS attribution report. If you need release-grade legal completeness, replace it with an automatically generated license inventory.",
                color = Color.White.copy(alpha = 0.62f)
            )

            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun OpenSourceLicensesScreenPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            OpenSourceLicensesScreen(
                navController = rememberNavController()
            )
        }
    }
}
