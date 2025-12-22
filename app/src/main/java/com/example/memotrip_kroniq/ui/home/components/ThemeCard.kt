package com.example.memotrip_kroniq.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.*

@Composable
fun ThemeCard(
    modifier: Modifier = Modifier,
    title: String,
    imageRes: Int,
    locked: Boolean = true      // 🔒 výchozí stav: zamčeno
) {
    val s = LocalUiScaler.current

    Box(
        modifier = modifier
            .height(150f.sy(s))
    ) {
        // --- IMAGE POZADÍ (vždy dole)---
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .alpha(if (locked) 0.80f else 0.90f),
            contentScale = ContentScale.Crop
        )

        // --- ZÁMEK (pokud je locked = true) ---
        if (locked) {
            Image(
                painter = painterResource(R.drawable.homescreen_ic_lock_theme),
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)         // 🔥 menší velikost jako Figma
                    .align(Alignment.Center)
                    .offset(y = 25.dp),        // 🔥 posun níž a víc dovnitř
                alpha = 0.80f                  // 🔥 jemné zesvětlení jako Figma
            )
        }
    }
}


