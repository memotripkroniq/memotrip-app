package com.example.memotrip_kroniq.ui.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    val s = LocalUiScaler.current

    Text(
        text = title,
        color = Color.White,
        fontSize = 16f.fs(s),
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(top = 6.dp, start = 2.dp, bottom = 2.dp)
    )
}