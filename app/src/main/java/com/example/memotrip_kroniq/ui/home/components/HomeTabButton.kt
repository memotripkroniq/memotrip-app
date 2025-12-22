package com.example.memotrip_kroniq.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.core.fs

@Composable
fun HomeTabButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val s = LocalUiScaler.current

    val bg = if (selected) Color(0xFF0077C8) else Color.Transparent
    val border = if (selected) Color(0xFF0077C8) else Color.White

    Card(
        modifier = modifier.height(40f.sy(s)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, border),
        colors = CardDefaults.cardColors(containerColor = bg),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 14f.fs(s)
                )

                Spacer(Modifier.width(6f.sx(s)))

                // 🔒 zámek u každého tab buttonu
                Image(
                    painter = painterResource(R.drawable.homescreen_ic_lock_theme),
                    contentDescription = "Locked",
                    modifier = Modifier.size(14f.sx(s))
                )
            }
        }
    }
}
