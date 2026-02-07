package com.example.memotrip_kroniq.ui.tripdetail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.model.ThemeType
import com.example.memotrip_kroniq.ui.addtrip.components.AddTripThemeCard
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.theme.AppTheme
// TODO: -----------------------------------------------------------------------------------
// TODO: Legacy ThemeSection – currently unused (replaced by ThemeSelector for consistency)
// TODO: -----------------------------------------------------------------------------------

data class ThemeUi(
    val type: ThemeType,
    val imageRes: Int
)

@Composable
fun ThemeSection(
    selected: ThemeType?,
    locked: Boolean,
    themes: List<ThemeUi>,
    onSelect: (ThemeType) -> Unit
) {
    val s = LocalUiScaler.current

    Column(
        modifier = Modifier
    ) {

        // Header: "Theme" + lock (stejně jako u tebe)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Theme",
                color = Color.White,
                fontSize = 16f.fs(s),
                fontWeight = FontWeight.Bold
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

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10f.sx(s))
        ) {
            items(
                items = themes,
                key = { it.type.name }
            ) { theme ->
                AddTripThemeCard(
                    imageRes = theme.imageRes,
                    locked = locked,
                    selected = selected == theme.type,
                    modifier = Modifier,
                )

                // klik pro select (jen když není locked)
                // Pozor: AddTripThemeCard nemá onClick, takže klik řešíme obalením:
                // ale teď necháme zatím bez kliků, aby byl krok malý.
            }
        }

        Spacer(Modifier.height(4f.sy(s)))
    }
}

@Preview(
    name = "ThemeSection - Locked",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 120
)
@Composable
private fun ThemeSectionPreviewLocked() {
    AppTheme {
        ThemeSection(
            selected = ThemeType.SUMMER,
            locked = true,
            themes = listOf(
                ThemeUi(ThemeType.SUMMER, R.drawable.homescreen_theme_summer),
                ThemeUi(ThemeType.WINTER, R.drawable.homescreen_theme_winter),
                ThemeUi(ThemeType.CAMPING, R.drawable.homescreen_theme_camping),
                ThemeUi(ThemeType.CITIES, R.drawable.homescreen_theme_cities),
                ThemeUi(ThemeType.NATURE, R.drawable.homescreen_theme_nature)
            ),
            onSelect = {}
        )
    }
}

@Preview(
    name = "ThemeSection - Unlocked",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 120
)
@Composable
private fun ThemeSectionPreviewUnlocked() {
    AppTheme {
        ThemeSection(
            selected = ThemeType.SUMMER,
            locked = false,
            themes = listOf(
                ThemeUi(ThemeType.SUMMER, R.drawable.homescreen_theme_summer),
                ThemeUi(ThemeType.WINTER, R.drawable.homescreen_theme_winter),
                ThemeUi(ThemeType.CAMPING, R.drawable.homescreen_theme_camping),
                ThemeUi(ThemeType.CITIES, R.drawable.homescreen_theme_cities),
                ThemeUi(ThemeType.NATURE, R.drawable.homescreen_theme_nature)
            ),
            onSelect = {}
        )
    }
}
