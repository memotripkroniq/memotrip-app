package com.example.memotrip_kroniq.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerShadow

@Composable
fun CustomInputBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    error: String? = null,
    showError: Boolean,
    onFocus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val s = LocalUiScaler.current
    var passwordVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    val borderColor =
        if (error != null) Color(0xFF759F67)
        else Color(0xFF747781)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(45f.sy(s))
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF353D4E))
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .innerShadow(
                color = Color.White,
                offsetX = 0f,
                offsetY = 1f,
                blur = 2f,
                cornerRadius = 10f
            )
            .padding(horizontal = 16f.sx(s)),
        contentAlignment = Alignment.CenterStart
    ) {

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = Color.White,
                fontSize = 16f.fs(s)
            ),
            cursorBrush = SolidColor(Color.White),
            visualTransformation =
                if (isPassword && !passwordVisible)
                    PasswordVisualTransformation()
                else
                    VisualTransformation.None,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (it.isFocused) onFocus()
                }
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusRequester.requestFocus()
                    }
                }
                .padding(end = if (isPassword) 28f.sx(s) else 0.dp),

            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {

                    when {
                        // 🔴 ERROR – jen pokud není fokus a není co editovat
                        error != null && showError -> {
                            Text(
                                text = error,
                                color = Color(0xFF759F67),
                                fontSize = 16f.fs(s)
                            )
                        }

                        // 🟡 PLACEHOLDER
                        value.isEmpty() -> {
                            Text(
                                text = label,
                                color = Color(0xFFB5BEC7),
                                fontSize = 16f.fs(s)
                            )
                        }
                    }

                    // ✍️ TEXTFIELD – vždy existuje (kvůli IME)
                    innerTextField()
                }
            }
        )

        if (isPassword) {
            Image(
                painter = painterResource(
                    if (passwordVisible)
                        R.drawable.ic_visibility
                    else
                        R.drawable.ic_visibility_off
                ),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(22f.sx(s))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                passwordVisible = !passwordVisible
                            }
                        )
                    }
            )
        }
    }
}

