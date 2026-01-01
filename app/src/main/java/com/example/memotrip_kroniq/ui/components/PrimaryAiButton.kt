package com.example.memotrip_kroniq.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.topOverlayShadow

@Composable
fun PrimaryAiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    iconStartPadding: Dp = 18.dp
) {
    Box(
        modifier = modifier
            .width(150.dp)
            .height(45.dp)
            .clip(RoundedCornerShape(10.dp))   // 🔥 ořez
            .topOverlayShadow(                // 🔥 top inner shadow
                alpha = 0.25f,
                heightDp = 8f
            )
    ) {
        Button(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF383A41),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF383A41),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = iconStartPadding,
                        end = 12.dp
                    )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_ai_bubbles),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}


@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
private fun PrimaryAiButtonPreview() {
    PrimaryAiButton(
        text = "Generate",
        onClick = {},
        isLoading = false
    )
}

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
private fun PrimaryAiButtonLoadingPreview() {
    PrimaryAiButton(
        text = "Generating…",
        onClick = {},
        isLoading = true
    )
}

