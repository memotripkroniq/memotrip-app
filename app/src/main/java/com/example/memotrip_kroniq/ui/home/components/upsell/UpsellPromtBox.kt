package com.example.memotrip_kroniq.ui.home.components.upsell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memotrip_kroniq.R

@Composable
fun UpsellPromptBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF383A41)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.upsell_prompt_more_trips),
            color = Color(0xFF79C46B),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 0.3.sp
        )
    }
}
