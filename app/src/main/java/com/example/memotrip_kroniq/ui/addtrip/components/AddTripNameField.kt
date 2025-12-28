package com.example.memotrip_kroniq.ui.addtrip.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.memotrip_kroniq.R

@Composable
fun AddTripNameField(
    value: String,
    coverPhotoUri: Uri?,
    onValueChange: (String) -> Unit,
    onAddPhotoClick: () -> Unit,
    error: Boolean,
    showError: Boolean
) {
    var hasFocus by remember { mutableStateOf(false) }

    // 🔴 CHANGE 1: vlastní TextFieldValue – držíme text + selection
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(value))
    }

    // 🔴 CHANGE 2: synchronizace textu zvenku (ViewModel → UI)
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = value)
        }
    }

    // 🔴 CHANGE 3: selection NA KONEC po získání focusu (po 1. frame)
    LaunchedEffect(hasFocus) {
        if (hasFocus) {
            textFieldValue = textFieldValue.copy(
                selection = TextRange(textFieldValue.text.length)
            )
        }
    }

    val errorGreen = Color(0xFF759F67)
    val borderColor =
        if (error && showError) errorGreen else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF383A41))
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(start = 4.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (coverPhotoUri != null) {
            AsyncImage(
                model = coverPhotoUri,
                contentDescription = null,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onAddPhotoClick() },
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.add_trip_name_field),
                contentDescription = null,
                tint = Color(0xFF747781).copy(alpha = 0.8f),
                modifier = Modifier
                    .size(38.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onAddPhotoClick() }
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = textFieldValue, // 🔴 CHANGE 4: TextFieldValue místo String
            onValueChange = {
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
                .weight(1f)
                .onFocusChanged { hasFocus = it.isFocused }, // 🔴 CHANGE 5
            decorationBox = { innerTextField ->
                when {
                    hasFocus -> {
                        // ✍️ EDIT MODE – vždy BasicTextField (cursor!)
                        innerTextField()
                    }

                    textFieldValue.text.isEmpty() -> {
                        // PLACEHOLDER (bez focusu)
                        Text(
                            text = "Add Trip name",
                            color = if (error && showError)
                                errorGreen
                            else
                                Color.White.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }

                    else -> {
                        // 👁️ VIEW MODE – ellipsis
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
