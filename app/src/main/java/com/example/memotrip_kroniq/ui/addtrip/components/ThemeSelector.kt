package com.example.memotrip_kroniq.ui.addtrip.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.model.ThemeType

@Composable
fun ThemeSelector(
    selected: ThemeType?,
    locked: Boolean,
    onSelect: (ThemeType) -> Unit
) {
    // 🔹 HLAVIČKA "Theme + 🔒" (už ji máš hotovou)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.trip_detail_theme),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (locked) {
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(R.drawable.homescreen_ic_lock_theme),
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .padding(bottom = 1.dp)
            )
        }
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ThemeType.entries) { theme ->
            AddTripThemeCard(
                modifier = Modifier.clickable(
                    enabled = !locked,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onSelect(theme) },
                imageRes = theme.imageRes,
                locked = locked,
                selected = theme == selected
            )
        }
    }

}

