package com.example.memotrip_kroniq.ui.auth

import PreviewUiScaler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.clip
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.data.AuthRepository

import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.components.SocialIcon
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.core.topOverlayShadow
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onCreateAccount: () -> Unit = {},
    onLoginSuccess: (String) -> Unit = {},
    onGoogleLoginSuccess: (String) -> Unit = {},
    onForgot: () -> Unit = {}
) {
    val s = LocalUiScaler.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // 🔐 JEDEN TokenDataStore
    val tokenStore = remember { TokenDataStore(context) }

    // 🔌 Repository (už s tokenStore)
    val repository = remember {
        AuthRepository(
            api = RetrofitClient.api,
            tokenStore = tokenStore
        )
    }

    // 🧠 ViewModel – správně přes viewModel()
    val viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(
                    repository = repository,
                    tokenStore = tokenStore
                ) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()

    // =========================
    // INPUT STATE
    // =========================
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var submitted by remember { mutableStateOf(false) }

    // =========================
    // BE ERROR HANDLING  ✅ OPRAVA
    // =========================
    LaunchedEffect(state) {
        when (val st = state) {

            is LoginState.Error -> {
                submitted = true   // 🔴 DŮLEŽITÉ

                // reset FE errorů
                //emailError = null
                //passwordError = null

                when (st.message) {

                    "You must be registered" -> {
                        email = ""                 // 🔥 TADY se maže email
                        password = ""
                        emailError = st.message    // 🟢 error do emailu
                    }

                    "Incorrect password" -> {
                        passwordError = st.message // 🟢 error do passwordu
                        if (st.clearPassword) password = ""
                    }

                    "This account uses Google login" -> {
                        email = ""
                        password = ""
                        emailError = st.message
                    }

                    else -> {
                        password = ""
                        emailError = "Login failed"
                    }
                }
            }

            is LoginState.Success -> {
                submitted = false
                emailError = null
                passwordError = null
                onLoginSuccess(st.user.id)
            }

            else -> Unit
        }
    }


    // =========================
    // GOOGLE LOGIN
    // =========================
    val googleClient: GoogleSignInClient = remember { createGoogleSignInClient(context) }

    val googleLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data ?: return@rememberLauncherForActivityResult
            try {
                val account = GoogleSignIn
                    .getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)

                account.idToken?.let {
                    viewModel.loginWithGoogle(it)
                    onGoogleLoginSuccess(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    // =========================
    // UI
    // =========================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(590f.sy(s))
            ) {

                Image(
                    painter = painterResource(R.drawable.login_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 75f.sy(s)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Image(
                        painter = painterResource(R.drawable.ic_logo_memotrip),
                        contentDescription = null,
                        modifier = Modifier.size(210f.sx(s))
                    )

                    Spacer(modifier = Modifier.height(26f.sy(s)))

                    LoginFieldsBox(
                        email = email,
                        password = password,
                        emailError = if (submitted) emailError else null,
                        passwordError = if (submitted) passwordError else null,
                        submitted = submitted,
                        onEmailChange = {
                            email = it
                            //if (submitted) emailError = null
                        },
                        onPasswordChange = {
                            password = it
                            //if (submitted) passwordError = null
                        },
                        onEmailFocus = {
                            if (submitted) emailError = null
                        },
                        onPasswordFocus = {
                            if (submitted) passwordError = null
                        },
                        onSignIn = {
                            submitted = true

                            val feEmailError = Validators.validateEmail(email)
                            val fePasswordError = Validators.validatePassword(password)

                            emailError = feEmailError
                            passwordError = fePasswordError

                            // 🔥 DŮLEŽITÉ – vymazat inputy při FE chybě
                            if (feEmailError != null && email.isNotEmpty()) {
                                email = ""
                            }

                            if (fePasswordError != null && password.isNotEmpty()) {
                                password = ""
                            }

                            // ❌ STOP – FE chyba
                            if (feEmailError != null || fePasswordError != null) return@LoginFieldsBox

                            // ✅ BE LOGIN
                            coroutineScope.launch {
                                viewModel.login(email, password)
                            }
                        },
                        onForgot = {
                            onForgot()
                        },
                        modifier = Modifier
                            .width(320f.sx(s))
                            .height(263f.sy(s))
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(30f.sy(s)))
                Text("or sign in with", color = Color.White, fontSize = 14f.fs(s))
                Spacer(modifier = Modifier.height(15f.sy(s)))

                Row(horizontalArrangement = Arrangement.spacedBy(20f.sx(s))) {
                    SocialIcon(
                        R.drawable.ic_google,
                        40f,
                        onClick = {
                            // 🔥 RESET FE VALIDACÍ
                            submitted = false
                            emailError = null
                            passwordError = null
                            email = ""
                            password = ""

                            googleLauncher.launch(googleClient.signInIntent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(35f.sy(s)))

                PrimaryButton(
                    text = "Create Account",
                    modifier = Modifier
                        .width(200f.sx(s)),
                    onClick = onCreateAccount
                )

            }
        }

        if (state is LoginState.Loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun LoginPreview() {
    CompositionLocalProvider(LocalUiScaler provides PreviewUiScaler) {
        MemoTripTheme {
            LoginScreen()
        }
    }
}
