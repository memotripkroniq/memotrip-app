package com.example.memotrip_kroniq.ui.auth

import PreviewUiScaler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.components.SocialIcon
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException

@Composable
fun SignUpScreen(
    onSuccess: (String) -> Unit = {},
) {
    val s = LocalUiScaler.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // 🔐 JEDEN TokenDataStore
    val tokenStore = remember { TokenDataStore(context) }

    // 🔌 Repository (už s tokenStore)
    val repository = remember {
        AuthRepository(
            api = RetrofitClient.api,
            tokenStore = tokenStore
        )
    }

    // 🧠 ViewModel – lifecycle-safe (STEJNĚ JAKO LOGIN)
    val viewModel: SignupViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SignupViewModel(
                    repository = repository,
                    tokenStore = tokenStore
                ) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    var submitted by remember { mutableStateOf(false) }

    var legalChecked by remember { mutableStateOf(false) }
    var legalError by remember { mutableStateOf<String?>(null) }

    // =====================================
    //  ERROR HANDLING
    // =====================================
    LaunchedEffect(state) {
        if (state is SignupState.Error) {
            val msg = (state as SignupState.Error).message

            when (msg) {
                "Passwords do not match" -> {
                    confirmPassword = ""
                    confirmError = msg
                }

                "Email already exists" -> {
                    email = ""
                    emailError = msg
                }

                else -> {
                    emailError = msg
                }
            }
        }

        if (state is SignupState.Success) {
            onSuccess((state as SignupState.Success).token)
        }
    }

    // =========================
    // GOOGLE LOGIN
    // =========================
    val googleClient: GoogleSignInClient = remember {
        createGoogleSignInClient(context)
    }

    val googleLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result.data ?: return@rememberLauncherForActivityResult
            try {
                val account = GoogleSignIn
                    .getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)

                account.idToken?.let {
                    viewModel.signupWithGoogle(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                viewModel.setError("Google signup failed")
            }
        }


    // =========================
    // UI
    // =========================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            }
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(680f.sy(s))
        ) {
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

                SignUpFieldsBox(
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmError = confirmError,
                    submitted = submitted,
                    legalChecked = legalChecked,
                    onLegalCheckedChange = {
                        legalChecked = it
                        if (submitted) legalError = null
                    },
                    legalError = legalError,
                    onEmailChange = {
                        email = it
                        if (submitted) emailError = null
                    },
                    onPasswordChange = {
                        password = it
                        if (submitted) passwordError = null
                    },
                    onConfirmPasswordChange = {
                        confirmPassword = it
                        if (submitted) confirmError = null
                    },
                    onEmailFocus = {
                        if (submitted) emailError = null
                    },
                    onPasswordFocus = {
                        if (submitted) passwordError = null
                    },

                    onSignUp = {
                        submitted = true   // 🔥 SPOUŠTÍ VALIDACI

                        // =========================
                        // FE VALIDACE
                        // =========================
                        val feEmailError = Validators.validateEmail(email)
                        val fePasswordError = Validators.validatePassword(password)

                        emailError = feEmailError
                        passwordError = fePasswordError

                        confirmError =
                            when {
                                confirmPassword.isBlank() ->
                                    "Confirm password is required"

                                fePasswordError != null ->
                                    fePasswordError   // 🔥 stejná chyba jako heslo

                                password != confirmPassword ->
                                    "Passwords do not match" else -> null
                            }
                        legalError =
                            if (!legalChecked) "You must agree our Legal & Policies" else null

                        // =========================
                        // MAZÁNÍ INPUTŮ PŘI CHYBĚ
                        // =========================
                        if (feEmailError != null) email = ""
                        if (fePasswordError != null) password = ""
                        if (confirmError != null) confirmPassword = ""

                        // =========================
                        // STOP – FE CHYBA
                        // =========================
                        if (
                            feEmailError != null ||
                            fePasswordError != null ||
                            confirmError != null ||
                            legalError != null
                        ) return@SignUpFieldsBox

                        // =========================
                        // BE SIGNUP
                        // =========================
                        viewModel.signup(
                            email = email,
                            password = password,
                            name = null
                        )
                    },

                    modifier = Modifier
                        .width(320f.sx(s))
                        .wrapContentHeight()
                )
            }
        }


        // =====================================
        //  SOCIAL LOGIN PANEL
        // =====================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16f.sy(s)))

            Text(
                text = "or sign up with",
                color = Color.White,
                fontSize = 14f.fs(s)
            )

            Spacer(modifier = Modifier.height(12f.sy(s)))

            Row(horizontalArrangement = Arrangement.spacedBy(20f.sx(s))) {

                // GOOGLE LOGIN CALLBACK ✔️
                SocialIcon(
                    icon = R.drawable.ic_google,
                    sizePx = 40f,
                    onClick = {
                        submitted = false
                        emailError = null
                        passwordError = null
                        confirmError = null
                        legalError = null

                        googleLauncher.launch(googleClient.signInIntent)
                    }
                )
            }
        }
    }

    // 🔥 FULLSCREEN LOADER
    if (state is SignupState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Preview(widthDp = 412, heightDp = 892, showBackground = true)
@Composable
fun SignUpPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            SignUpScreen()
        }
    }
}
