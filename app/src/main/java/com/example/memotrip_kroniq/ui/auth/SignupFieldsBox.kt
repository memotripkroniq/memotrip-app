package com.example.memotrip_kroniq.ui.auth

import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.ui.components.MiniRadioButton
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.components.CustomInputBox
import com.example.memotrip_kroniq.ui.core.topOverlayShadow
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerShadow

@Composable
fun SignUpFieldsBox(
    email: String,
    password: String,
    confirmPassword: String,
    emailError: String?,
    passwordError: String?,
    confirmError: String?,
    submitted: Boolean,
    legalChecked: Boolean,
    legalError: String?,
    onLegalCheckedChange: (Boolean) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmailFocus: () -> Unit,
    onPasswordFocus: () -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalUiScaler.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .innerShadow(
                color = Color.White,
                offsetX = 0f,
                offsetY = 1f,
                blur = 2f,
                cornerRadius = 10f
            )
            .border(
                width = 1.5.dp,
                color = Color(0xFF747781),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(15f.sx(s))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10f.sy(s)),
            verticalArrangement = Arrangement.spacedBy(16f.sy(s)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Email
            CustomInputBox(
                label = "Email",
                value = email,
                error = emailError,
                showError = submitted,
                onValueChange = onEmailChange
            )

            // Password
            CustomInputBox(
                label = "Password",
                value = password,
                isPassword = true,
                error = passwordError,
                showError = submitted,
                onValueChange = onPasswordChange,
            )

            // Confirm Password
            CustomInputBox(
                label = "Confirm Password",
                value = confirmPassword,
                isPassword = true,
                error = confirmError,
                showError = submitted,
                onValueChange = onConfirmPasswordChange,
            )

            Spacer(modifier = Modifier.width(15f.sx(s)))

            // Radio + text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                MiniRadioButton(
                    selected = legalChecked,
                    onClick = {
                        onLegalCheckedChange(!legalChecked)
                    }
                )


                Text(
                    text = "By signing up, you agree our\nLegal & Policies.",
                    color = Color(0xFFFFFFFF),
                    fontSize = 14f.fs(s),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(220f.sx(s))
                )
            }

            // ⭐ vždy rezervuj místo pro legalError → UI se nebude hýbat
            Box(
                modifier = Modifier
                    .height(18f.sy(s))   // stabilní výška pole
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (submitted) {
                    legalError?.let {
                        Text(
                            text = it,
                            color = Color(0xFF759F67),
                            fontSize = 14f.fs(s)
                        )
                    }
                }
            }

            // SIGN UP BUTTON + LOADER
            Box(
                modifier = Modifier
                    .width(220f.sx(s))
                    .height(40f.sy(s))
                    .clip(RoundedCornerShape(8.dp))
                    .topOverlayShadow()
            ) {
                Button(
                    onClick = onSignUp,
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0077C8),
                        disabledContainerColor = Color(0xFF0077C8).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Sign up", fontSize = 16f.fs(s))
                }
            }
        }
    }
}

