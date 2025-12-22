package com.example.memotrip_kroniq.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.*

@Composable
fun ThemesGrid(
    locked: Boolean     // 🔥 nový parametr – globální lock/unlock
) {
    val s = LocalUiScaler.current
    val spacingX = 14f.sx(s)

    Column(
        verticalArrangement = Arrangement.spacedBy(14f.sy(s)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacingX)
        ) {
            ThemeCard(
                modifier = Modifier.weight(1f),
                title = "Summer",
                imageRes = R.drawable.homescreen_theme_summer,
                locked = locked
            )
            ThemeCard(
                modifier = Modifier.weight(1f),
                title = "Winter",
                imageRes = R.drawable.homescreen_theme_winter,
                locked = locked
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacingX)
        ) {
            ThemeCard(
                modifier = Modifier.weight(1f),
                title = "Camping",
                imageRes = R.drawable.homescreen_theme_camping,
                locked = locked
            )
            ThemeCard(
                modifier = Modifier.weight(1f),
                title = "Cities",
                imageRes = R.drawable.homescreen_theme_cities,
                locked = locked
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacingX)
        ) {
            ThemeCard(
                modifier = Modifier.weight(1f),
                title = "Nature",
                imageRes = R.drawable.homescreen_theme_nature,
                locked = locked
            )
            ThemeCard(
                modifier = Modifier.weight(1f),
                title = "Exotic",
                imageRes = R.drawable.homescreen_theme_exotic,
                locked = locked
            )
        }
    }
}



