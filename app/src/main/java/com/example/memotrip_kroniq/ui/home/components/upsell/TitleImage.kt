package com.example.memotrip_kroniq.ui.home.components.upsell

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun TitleImage(
    resId: Int,
    contentDescription: String
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        modifier = Modifier
            .height(30.dp)
            .padding(top = 2.dp),
        contentScale = ContentScale.Fit
    )
}