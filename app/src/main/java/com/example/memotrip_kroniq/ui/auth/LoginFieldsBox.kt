package com.example.memotrip_kroniq.ui.auth

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.ui.components.CustomInputBox
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.core.topOverlayShadow

@Composable
fun LoginFieldsBox(
    email: String,
    password: String,
    emailError: String?,
    passwordError: String?,
    submitted: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmailFocus: () -> Unit,
    onPasswordFocus: () -> Unit,
    onSignIn: () -> Unit,
    onForgot: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalUiScaler.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, Color(0xFF747781), RoundedCornerShape(10.dp))
            .padding(15f.sx(s))
    ) {

        Column(
            modifier = Modifier.padding(top = 11f.sy(s)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ==========================
            // EMAIL
            // ==========================
            CustomInputBox(
                label = "Email",
                value = email,
                error = emailError,
                showError = submitted,
                onValueChange = onEmailChange,
                onFocus = onEmailFocus
            )

            Spacer(Modifier.height(16f.sy(s)))

            // ==========================
            // PASSWORD
            // ==========================
            CustomInputBox(
                label = "Password",
                value = password,
                isPassword = true,
                error = passwordError,
                showError = submitted,
                onValueChange = onPasswordChange,
                onFocus = onPasswordFocus
            )

            Spacer(Modifier.height(28f.sy(s)))

            Text(
                text = "Forgot password",
                color = Color.White,
                fontSize = 14f.fs(s),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onForgot() }
            )

            Spacer(Modifier.height(28f.sy(s)))

            // ==========================
            // SIGN IN BUTTON (PrimaryButton)
            // ==========================
            PrimaryButton(
                text = "Sign in",
                modifier = Modifier.width(200f.sx(s)),
                onClick = {
                    println("🔥 SIGN IN CLICKED")
                    onSignIn()
                }
            )

        }
    }
}

