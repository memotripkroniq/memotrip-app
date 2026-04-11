package com.example.memotrip_kroniq.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars

@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    title: String,
    showBack: Boolean,
    centerTitle: Boolean = true,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null
) {
    val s = LocalUiScaler.current

    val topInset = WindowInsets.systemBars
        .asPaddingValues()
        .calculateTopPadding()

    val sideSlotWidth = 30f.sx(s)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(
                top = topInset + 40.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp
            )
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier.width(sideSlotWidth),
            contentAlignment = Alignment.CenterStart
        ) {
            if (showBack && onBackClick != null) {
                Image(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.common_back),
                    modifier = Modifier
                        .size(22f.sx(s))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBackClick() }
                )
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = if (centerTitle) Alignment.Center else Alignment.CenterStart
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 24f.fs(s),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = if (centerTitle) TextAlign.Center else TextAlign.Start
            )
        }

        Box(
            modifier = Modifier.width(sideSlotWidth),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (onMenuClick != null) {
                Image(
                    painter = painterResource(R.drawable.homescreen_ic_navigation_menu),
                    contentDescription = stringResource(R.string.common_menu),
                    modifier = Modifier
                        .size(28f.sx(s))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onMenuClick() }
                )
            }
        }
    }
}
