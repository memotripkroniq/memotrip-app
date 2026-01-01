package com.example.memotrip_kroniq.ui.addtrip.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun PrimaryCreateButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3),
            disabledContainerColor = Color(0xFF2B2E34),
            contentColor = Color.White,
            disabledContentColor = Color.Gray
        )
    ) {
        Text(
            text = "Next",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryCreateButtonEnabledPreview() {
    MemoTripTheme {
        PrimaryCreateButton(
            enabled = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryCreateButtonDisabledPreview() {
    MemoTripTheme {
        PrimaryCreateButton(
            enabled = false,
            onClick = {}
        )
    }
}
