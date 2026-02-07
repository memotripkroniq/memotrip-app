package com.example.memotrip_kroniq.ui.tripdetail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerTopShadow
import com.example.memotrip_kroniq.ui.theme.AppTheme

@Composable
fun ShareInKroniqSection(
    checked: Boolean,
    locked: Boolean,
    onToggle: () -> Unit
) {
    val s = LocalUiScaler.current

    Column(
        modifier = Modifier
    ) {

        // 🔹 Header: "KroniQ" + lock (jen pokud locked)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "KroniQ",
                color = Color.White,
                fontSize = 16f.fs(s),
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

        // 🔹 Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF383A41), RoundedCornerShape(10.dp))
                .innerTopShadow(alpha = 0.18f, height = 18f)
                .padding(start = 14.dp, end = 5.dp, top = 10.dp, bottom = 10.dp)
        ) {

            // 🔹 Content (row + hint) posunutý lehce nahoru
            Column(
                modifier = Modifier.offset(y = (-2).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // LEFT: icon + label
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.tripdetail_ic_share),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = "Share in KroniQ",
                            color = Color.White,
                            fontSize = 16f.fs(s),
                            fontWeight = FontWeight.Normal,
                            maxLines = 1
                        )
                    }

                    // RIGHT: smaller switch
                    Switch(
                        checked = checked,
                        onCheckedChange = { if (!locked) onToggle() },
                        enabled = !locked,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .scale(0.75f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0077C8),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF5B5E66),
                            disabledCheckedTrackColor = Color(0xFF5B5E66),
                            disabledUncheckedTrackColor = Color(0xFF5B5E66),
                            disabledCheckedThumbColor = Color.White.copy(alpha = 0.7f),
                            disabledUncheckedThumbColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }

                Spacer(Modifier.height(4f.sy(s)))

                Text(
                    text = "Choose a theme to make this trip visible in KroniQ",
                    color = Color(0xFF759F67),
                    fontSize = 14f.fs(s),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Preview(
    name = "ShareInKroniq - Locked",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412
)
@Composable
private fun ShareInKroniqLockedPreview() {
    AppTheme {
        ShareInKroniqSection(
            checked = false,
            locked = true,
            onToggle = {}
        )
    }
}

@Preview(
    name = "ShareInKroniq - Unlocked",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412
)
@Composable
private fun ShareInKroniqUnlockedPreview() {
    AppTheme {
        ShareInKroniqSection(
            checked = true,
            locked = false,
            onToggle = {}
        )
    }
}
