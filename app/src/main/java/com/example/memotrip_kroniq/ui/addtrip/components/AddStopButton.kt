package com.example.memotrip_kroniq.ui.addtrip.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun AddStopButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    Row(
        modifier = Modifier
            .wrapContentWidth()
            .padding(top = 5.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = stringResource(R.string.add_trip_add_stop),
            color = Color.White
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(30.dp)
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_wp_add),
                contentDescription = stringResource(R.string.add_trip_add_stop),
                tint = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun AddStopButtonPreview() {
    MemoTripTheme {
        AddStopButton(
            visible = true,
            onClick = {}
        )
    }
}
