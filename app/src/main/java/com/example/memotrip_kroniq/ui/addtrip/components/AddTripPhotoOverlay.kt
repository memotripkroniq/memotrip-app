package com.example.memotrip_kroniq.ui.addtrip.components

import PreviewUiScaler
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import innerShadow
import androidx.compose.ui.platform.LocalInspectionMode



@Composable
fun AddTripPhotoOverlay(
    canDelete: Boolean,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onDeletePhoto: () -> Unit,
    onDismiss: () -> Unit
) {
    val isPreview = LocalInspectionMode.current

    Box(modifier = Modifier.fillMaxSize()) {

        // SCRIM
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
                .then(
                    if (!isPreview)
                        Modifier.clickable { onDismiss() }
                    else
                        Modifier
                )
        )

        // CONTENT
        Box(
            modifier = Modifier
                .align(Alignment.Center)
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
        modifier = Modifier
            .widthIn(max = 320.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF383A41))
            .innerShadow()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Add your picture",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        PhotoActionButton(
            icon = R.drawable.ic_camera,
            text = "Take a photo",
            onClick = onTakePhoto
        )

        Spacer(Modifier.height(10.dp))

        PhotoActionButton(
            icon = R.drawable.ic_gallery,
            text = "Choose from your files",
            onClick = onPickFromGallery
        )

        Spacer(Modifier.height(10.dp))

        PhotoActionButton(
            icon = R.drawable.ic_delete,
            text = "Delete photo",
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
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .then(
                if (enabled)
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClick() }
                else Modifier
            )
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .width(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color(0xFF383A41).copy(alpha = contentAlpha)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            color = Color.White.copy(alpha = contentAlpha),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
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
private fun AddTripPhotoOverlayPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        AddTripPhotoOverlay(
            canDelete = false,
            onTakePhoto = {},
            onPickFromGallery = {},
            onDeletePhoto = {},
            onDismiss = {}
        )
    }
}





