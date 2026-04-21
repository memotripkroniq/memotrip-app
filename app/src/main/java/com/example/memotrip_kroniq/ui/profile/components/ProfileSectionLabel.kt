package com.example.memotrip_kroniq.ui.profile.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sx

@Composable
fun ProfileSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    showLock: Boolean = false
) {
    val s = LocalUiScaler.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16f.fs(s),
            fontWeight = FontWeight.SemiBold
        )

        if (showLock) {
            Spacer(modifier = Modifier.width(8f.sx(s)))

            Image(
                painter = painterResource(R.drawable.homescreen_ic_lock_theme),
                contentDescription = null,
                modifier = Modifier.size(16f.sx(s))
            )
        }
    }
}