package com.example.memotrip_kroniq.ui.addtrip.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import coil.compose.AsyncImage


@Composable
fun AddTripNameField(
    value: String,
    coverPhotoUri: Uri?,
    onValueChange: (String) -> Unit,
    onAddPhotoClick: () -> Unit
) {
    var hasFocus by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp) // 🔥 Výška Add Trip name boxu
            .background(
                color = Color(0xFF383A41),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(start = 4.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (coverPhotoUri != null) {
            AsyncImage(
                model = coverPhotoUri,
                contentDescription = null,
                modifier = Modifier
                    .size(35.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onAddPhotoClick()
                    },
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
                    ) {
                        onAddPhotoClick()
                    }
            )
        }


        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { hasFocus = it.isFocused },
            decorationBox = { innerTextField ->
                when {
                    value.isEmpty() -> {
                        // PLACEHOLDER
                        Text(
                            text = "Add Trip name",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }

                    hasFocus -> {
                        // ✍️ EDIT MODE → cursor + scroll
                        innerTextField()
                    }

                    else -> {
                        // 👁️ VIEW MODE → ellipsis
                        Text(
                            text = value,
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



@Preview(showBackground = true)
@Composable
private fun AddTripNameFieldPreview() {
    MemoTripTheme {
        AddTripNameField(
            value = "",
            coverPhotoUri = null,
            onValueChange = {},
            onAddPhotoClick = {}
        )
    }
}
