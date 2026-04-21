package com.example.memotrip_kroniq.ui.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import androidx.compose.ui.unit.dp

@Composable
fun ProfileHeaderSection(
    photoModel: Any?,
    name: String,
    onPhotoClick: () -> Unit,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalUiScaler.current
    var hasFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(name))
    }

    LaunchedEffect(name) {
        if (name != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = name)
        }
    }

    LaunchedEffect(hasFocus) {
        if (hasFocus) {
            textFieldValue = textFieldValue.copy(
                selection = TextRange(textFieldValue.text.length)
            )
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (photoModel != null) {
            AsyncImage(
                model = photoModel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onPhotoClick() }
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.add_trip_name_field),
                contentDescription = null,
                tint = Color(0xFF747781).copy(alpha = 0.8f),
                modifier = Modifier
                    .size(82.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onPhotoClick() }
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    onNameChange(it.text)
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 24f.fs(s),
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .focusRequester(focusRequester)
                    .onFocusChanged { hasFocus = it.isFocused },
                decorationBox = { innerTextField ->
                    when {
                        hasFocus -> {
                            innerTextField()
                        }

                        textFieldValue.text.isEmpty() -> {
                            Text(
                                text = stringResource(R.string.profile_name_placeholder),
                                color = Color(0xFF8B8B8B),
                                fontSize = 24f.fs(s),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        else -> {
                            Text(
                                text = textFieldValue.text,
                                color = Color.White,
                                fontSize = 24f.fs(s),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                painter = painterResource(R.drawable.tripdetail_ic_edit),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusRequester.requestFocus()
                    }
            )
        }
    }
}
