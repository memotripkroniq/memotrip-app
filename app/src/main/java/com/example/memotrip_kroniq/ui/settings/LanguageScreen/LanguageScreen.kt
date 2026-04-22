package com.example.memotrip_kroniq.ui.settings

import PreviewUiScaler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.core.localization.AppLanguage
import com.example.memotrip_kroniq.core.localization.AppLocaleManager
import com.example.memotrip_kroniq.data.datastore.LanguageDataStore
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import kotlinx.coroutines.launch

@Composable
fun LanguageScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val s = LocalUiScaler.current
    val languageDataStore = remember(context) { LanguageDataStore(context) }
    val coroutineScope = rememberCoroutineScope()
    var selectedLanguage by remember { mutableStateOf(AppLocaleManager.getCurrentLanguage()) }

    LaunchedEffect(languageDataStore) {
        selectedLanguage = AppLocaleManager.getCurrentLanguage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AppTopBar(
            title = stringResource(R.string.language_title),
            showBack = true,
            onBackClick = { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            AppLanguage.entries.forEach { language ->
                LanguageOptionRow(
                    title = stringResource(language.labelRes),
                    selected = language == selectedLanguage,
                    onClick = {
                        if (language == selectedLanguage) return@LanguageOptionRow

                        selectedLanguage = language
                        coroutineScope.launch {
                            languageDataStore.saveLanguageTag(language.languageTag)
                            AppLocaleManager.applyLanguage(language.languageTag)
                            navController.popBackStack()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LanguageOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val s = LocalUiScaler.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(Color(0xFF383A41), RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16f.fs(s),
            modifier = Modifier.weight(1f)
        )

        if (selected) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(Color(0xFF1686D9), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 12f.fs(s),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun LanguageScreenPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            LanguageScreen(
                navController = rememberNavController()
            )
        }
    }
}
