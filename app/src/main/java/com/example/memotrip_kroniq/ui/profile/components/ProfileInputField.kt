package com.example.memotrip_kroniq.ui.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileInputField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: Boolean = false,
    enabled: Boolean = true
) {
    var hasFocus by remember { mutableStateOf(false) }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(value))
    }

    // sync z VM
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = value)
        }
    }

    // cursor na konec
    LaunchedEffect(hasFocus) {
        if (hasFocus) {
            textFieldValue = textFieldValue.copy(
                selection = TextRange(textFieldValue.text.length)
            )
        }
    }

    val errorGreen = Color(0xFF759F67)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF383A41))
            .border(
                width = 1.5.dp,
                color = if (error) errorGreen else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {

        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                if (!enabled) return@BasicTextField
                textFieldValue = it
                onValueChange(it.text)
            },
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { hasFocus = enabled && it.isFocused },
            decorationBox = { innerTextField ->
                when {
                    hasFocus && enabled -> {
                        innerTextField()
                    }

                    textFieldValue.text.isEmpty() -> {
                        Text(
                            text = placeholder,
                            color = if (error)
                                errorGreen
                            else
                                Color.White.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }

                    else -> {
                        Text(
                            text = textFieldValue.text,
                            color = Color.White,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        )
    }
}
