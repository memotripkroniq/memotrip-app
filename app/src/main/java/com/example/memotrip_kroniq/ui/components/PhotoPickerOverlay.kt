package com.example.memotrip_kroniq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrip_kroniq.R
import innerShadow

@Composable
fun PhotoPickerOverlay(
    canDelete: Boolean,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onDeletePhoto: () -> Unit,
    onDismiss: () -> Unit
) {
    val isPreview = LocalInspectionMode.current

    Box(modifier = Modifier.Companion.fillMaxSize()) {

        // SCRIM
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(Color.Companion.Black.copy(alpha = 0.25f))
                .then(
                    if (!isPreview)
                        Modifier.Companion.clickable { onDismiss() }
                    else
                        Modifier.Companion
                )
        )

        // CONTENT
        Box(
            modifier = Modifier.Companion
                .align(Alignment.Companion.Center)
                .padding(16.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            AddTripPhotoContent(
                canDelete = canDelete,
                onTakePhoto = onTakePhoto,
                onPickFromGallery = onPickFromGallery,
                onDeletePhoto = onDeletePhoto
            )
        }
    }
}


@Composable
private fun AddTripPhotoContent(
    canDelete: Boolean,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onDeletePhoto: () -> Unit
) {
    Column(
        modifier = Modifier.Companion
            .widthIn(max = 320.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(Color(0xFF383A41))
            .innerShadow()
            .padding(16.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.photo_picker_title),
            color = Color.Companion.White,
            fontWeight = FontWeight.Companion.SemiBold,
            fontSize = 20.sp,
            modifier = Modifier.Companion.padding(bottom = 16.dp)
        )

        PhotoActionButton(
            icon = R.drawable.ic_camera,
            text = stringResource(R.string.photo_picker_take_photo),
            onClick = onTakePhoto
        )

        Spacer(Modifier.Companion.height(10.dp))

        PhotoActionButton(
            icon = R.drawable.ic_gallery,
            text = stringResource(R.string.photo_picker_choose_from_files),
            onClick = onPickFromGallery
        )

        Spacer(Modifier.Companion.height(10.dp))

        PhotoActionButton(
            icon = R.drawable.ic_delete,
            text = stringResource(R.string.photo_picker_delete_photo),
            enabled = canDelete,
            isDestructive = true,
            onClick = onDeletePhoto
        )
    }
}

@Composable
private fun PhotoActionButton(
    icon: Int,
    text: String,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {

    val background = when {
        !enabled -> Color(0xFF759F67).copy(alpha = 0.6f )   // disabled
        else -> Color(0xFF759F67)       // enabled (všechny)
    }

    val contentAlpha = if (enabled) 1f else 0.4f

    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(48.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(background)
            .then(
                if (enabled)
                    Modifier.Companion.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClick() }
                else Modifier.Companion
            )
            .padding(horizontal = 16.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp)),
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {

        Box(
            modifier = Modifier.Companion
                .width(24.dp),
            contentAlignment = Alignment.Companion.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color(0xFF383A41).copy(alpha = contentAlpha)
            )
        }

        Spacer(modifier = Modifier.Companion.width(12.dp))

        Text(
            text = text,
            color = Color.Companion.White.copy(alpha = contentAlpha),
            fontSize = 16.sp,
            fontWeight = FontWeight.Companion.Bold
        )
    }
}


@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 892
)
@Composable
private fun PhotoPickerOverlayPreview() {
    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(Color.Companion.DarkGray)
    ) {
        PhotoPickerOverlay(
            canDelete = false,
            onTakePhoto = {},
            onPickFromGallery = {},
            onDeletePhoto = {},
            onDismiss = {}
        )
    }
}
