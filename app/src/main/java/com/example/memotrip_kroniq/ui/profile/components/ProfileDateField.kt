package com.example.memotrip_kroniq.ui.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs

@Composable
fun ProfileDateField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: Boolean = false
) {
    val s = LocalUiScaler.current
    val errorGreen = Color(0xFF759F67)
    val isEmpty = value.isBlank()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .background(
                color = Color(0xFF383A41),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.5.dp,
                color = if (error) errorGreen else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isEmpty) stringResource(R.string.profile_date_of_birth_placeholder) else value,
            color = when {
                error -> errorGreen
                isEmpty -> Color.Gray
                else -> Color.White
            },
            fontSize = 16f.fs(s),
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = stringResource(R.string.profile_select_date_of_birth),
            tint = Color(0xFF759F67)
        )
    }
}
