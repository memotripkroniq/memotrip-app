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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun AiFeaturesNoticeScreen(
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AppTopBar(
            title = stringResource(R.string.legal_ai_features_notice),
            showBack = true,
            onBackClick = { navController.popBackStack() },
            onMenuClick = null
        )

        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = stringResource(R.string.ai_notice_intro_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.ai_notice_intro_body),
                    color = Color.White.copy(alpha = 0.86f)
                )

                AiNoticeSection(
                    title = stringResource(R.string.ai_notice_usage_title),
                    body = stringResource(R.string.ai_notice_usage_body)
                )

                AiNoticeSection(
                    title = stringResource(R.string.ai_notice_inputs_title),
                    body = stringResource(R.string.ai_notice_inputs_body)
                )

                AiNoticeSection(
                    title = stringResource(R.string.ai_notice_outputs_title),
                    body = stringResource(R.string.ai_notice_outputs_body)
                )

                AiNoticeSection(
                    title = stringResource(R.string.ai_notice_accuracy_title),
                    body = stringResource(R.string.ai_notice_accuracy_body)
                )

                AiNoticeSection(
                    title = stringResource(R.string.ai_notice_safety_title),
                    body = stringResource(R.string.ai_notice_safety_body)
                )

                AiNoticeSection(
                    title = stringResource(R.string.ai_notice_exports_title),
                    body = stringResource(R.string.ai_notice_exports_body)
                )

                AiNoticeSection(
                    title = stringResource(R.string.ai_notice_improvements_title),
                    body = stringResource(R.string.ai_notice_improvements_body)
                )

                Text(
                    text = stringResource(R.string.ai_notice_footer_note),
                    color = Color.White.copy(alpha = 0.62f)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AiNoticeSection(
    title: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = body,
            color = Color.White.copy(alpha = 0.82f)
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun AiFeaturesNoticeScreenPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            AiFeaturesNoticeScreen(
                navController = rememberNavController()
            )
        }
    }
}
