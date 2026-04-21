package com.example.memotrip_kroniq.ui.auth

import PreviewUiScaler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.ui.components.CustomInputBox
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit = {}
) {
    val s = LocalUiScaler.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // 🔐 Token store
    val tokenStore = remember { TokenDataStore(context) }

    // 🔌 Repository
    val repository = remember {
        AuthRepository(
            api = RetrofitClient.authApi,
            tokenStore = tokenStore
        )
    }

    // 🧠 ViewModel – stejný pattern jako Login / Signup
    val viewModel: ForgotPasswordViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return ForgotPasswordViewModel(
                        authRepository = repository
                    ) as T
                }
            }
        )

    val state by viewModel.state

    // =========================
    // INPUT STATE
    // =========================
    var email by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    // =========================
    // UI
    // =========================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }
    ) {

        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                title = "",
                showBack = true,
                onBackClick = onBack
            )

            //Spacer(modifier = Modifier.height(16f.sy(s)))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(590f.sy(s))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 15f.sy(s)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(26f.sy(s)))

                    // =========================
                    // FORGOT PASSWORD CONTENT
                    // =========================
                    Column(
                        modifier = Modifier
                            .width(320f.sx(s))
                            .wrapContentHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = stringResource(R.string.auth_forgot_password),
                            fontSize = 32f.fs(s),
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        Spacer(Modifier.height(35f.sy(s)))

                        Text(
                            text = stringResource(R.string.auth_forgot_password_subtitle),
                            fontSize = 16f.fs(s),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(82f.sy(s)))

                        Text(
                            text = stringResource(R.string.auth_add_email),
                            fontSize = 16f.fs(s),
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8f.sy(s))
                        )

                        CustomInputBox(
                            label = stringResource(R.string.auth_enter_your_email),
                            value = email,
                            onValueChange = { email = it },
                            isPassword = false,
                            error = null,
                            showError = false
                        )

                        Spacer(Modifier.height(50f.sy(s)))

                        // ✅ BUTTON JE TEĎ VE STEJNÉM CONTENTU
                        PrimaryButton(
                            text = stringResource(R.string.auth_continue),
                            modifier = Modifier
                                .width(200f.sx(s))   // 🎯 Figma: 200 Fill
                                .height(40f.sy(s)),  // 🎯 Figma: 40 Hug
                            onClick = {
                                submitted = true
                                coroutineScope.launch {
                                    viewModel.submit(email)
                                }
                            }
                        )

                        Spacer(Modifier.height(50f.sy(s)))

                        if (state is ForgotPasswordState.Success && submitted) {
                            Text(
                                text = stringResource(R.string.auth_forgot_password_success),
                                fontSize = 16f.fs(s),
                                color = Color(0xFF759F67),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // =========================
            // BOTTOM FILLER
            // =========================
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        // =========================
        // LOADING OVERLAY
        // =========================
        if (state is ForgotPasswordState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun ForgotPasswordPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            ForgotPasswordScreen()
        }
    }
}
