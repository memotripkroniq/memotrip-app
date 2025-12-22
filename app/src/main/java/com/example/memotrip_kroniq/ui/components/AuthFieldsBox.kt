package com.example.memotrip_kroniq.ui.auth

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.components.CustomInputBox

@Composable
fun AuthFieldsBox(
    mode: AuthMode,
    email: String,
    password: String,
    confirmPassword: String = "",
    submitted: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onSubmit: () -> Unit
) {
    val s = LocalUiScaler.current
    var radioChecked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .border(1.dp, Color.White, shape = RoundedCornerShape(10.dp))
            .offset(16f.sx(s))
            .width(320f.sx(s))
            .height(
                if (mode == AuthMode.LOGIN) 263f.sy(s)
                else 330f.sy(s)  // vyšší kvůli radiobuttonu a 3 inputům
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15f.sy(s)),
            verticalArrangement = Arrangement.spacedBy(20f.sy(s))
        ) {

            CustomInputBox(
                label = "Email",
                value = email,
                showError = submitted,
                onValueChange = onEmailChange
            )

            CustomInputBox(
                label = "Password",
                value = password,
                onValueChange = onPasswordChange,
                showError = submitted,
                isPassword = true
            )

            if (mode == AuthMode.SIGNUP) {
                CustomInputBox(
                    label = "Confirm Password",
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    showError = submitted,
                    isPassword = true
                )

                // RADIO BUTTON – FUNKČNÍ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.offset(y = (-10f).sy(s))
                ) {
                    RadioButton(
                        selected = radioChecked,
                        onClick = { radioChecked = !radioChecked }
                    )
                    Spacer(modifier = Modifier.width(6f.sx(s)))
                    Text(
                        "By signing up, you agree to our Legal & Policies.",
                        color = Color.White,
                        fontSize = 13f.fs(s)
                    )
                }
            }

            if (mode == AuthMode.LOGIN) {
                TextButton(
                    onClick = onForgotPassword,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .offset(y = (-10f).sy(s))
                ) {
                    Text("Forgot Password", color = Color.White, fontSize = 15f.fs(s))
                }
            }
        }

        val buttonText = if (mode == AuthMode.LOGIN) "Sign in" else "Sign up"

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8f.sy(s))
                .width(200f.sx(s))
                .height(40f.sy(s)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077C8)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(buttonText, fontSize = 16f.fs(s))
        }
    }
}
