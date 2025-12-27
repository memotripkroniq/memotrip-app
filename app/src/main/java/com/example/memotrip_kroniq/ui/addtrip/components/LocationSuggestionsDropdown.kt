package com.example.memotrip_kroniq.ui.addtrip.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrip_kroniq.data.location.LocationSuggestion

@Composable
fun LocationSuggestionsDropdown(
    suggestions: List<LocationSuggestion>,
    onSelect: (LocationSuggestion) -> Unit
) {
    if (suggestions.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF2B2E34),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(vertical = 4.dp)
    ) {
        suggestions.forEach { item ->
            Text(
                text = item.displayName,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(item) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
