package com.example.memotrip_kroniq.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs

private val SettingsCardColor = Color(0xFF383A41)
private val ArrowGreen = Color(0xFF759F67)

@Composable
fun SettingsArrowItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalUiScaler.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SettingsCardColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16f.fs(s),
            modifier = Modifier.weight(1f),
            maxLines = 1
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = "Open settings option",
            tint = ArrowGreen,
            modifier = Modifier.size(20.dp)
        )
    }
}