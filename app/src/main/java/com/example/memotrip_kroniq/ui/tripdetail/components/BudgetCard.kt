package com.example.memotrip_kroniq.ui.tripdetail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerTopShadow
import com.example.memotrip_kroniq.ui.theme.AppTheme

@Composable
fun BudgetCard(
    plannedAmount: String,
    spentAmount: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    val ui = LocalUiScaler.current
    val cardColor = Color(0xFF383A41)
    val radius = 10.dp

    Column(
        modifier = Modifier
    ) {
        Text(
            text = "Budget",
            color = Color.White,
            fontSize = 16f.fs(ui),
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10f.sy(ui)))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(radius))
                .background(cardColor)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // LEFT: Planned
            BudgetInlineRow(
                label = "Planned:",
                value = plannedAmount,
                isVisible = isVisible
            )

            Spacer(Modifier.width(75.dp)) // ✅ větší mezera mezi Planned a Spent

            // MIDDLE: Spent
            BudgetInlineRow(
                label = "Spent:",
                value = spentAmount,
                isVisible = isVisible
            )

            Spacer(Modifier.weight(1f)) // vytlačí oko doprava

            // RIGHT: eye toggle
            Image(
                painter = painterResource(
                    if (isVisible)
                        R.drawable.ic_visibility
                    else
                        R.drawable.ic_visibility_off
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onToggleVisibility() }
            )
        }
    }
}



@Composable
private fun BudgetInlineRow(
    label: String,
    value: String,
    isVisible: Boolean
) {
    val ui = LocalUiScaler.current

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 16f.fs(ui)
        )

        Spacer(Modifier.width(12.dp)) // mezera mezi label a částkou

        Text(
            text = if (isVisible) value else "******",
            color = Color.White,
            fontSize = 16f.fs(ui),
            fontWeight = FontWeight.SemiBold
        )
    }
}



@Preview(
    name = "Budget - visible",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 160
)
@Composable
private fun BudgetCardPreview_Visible() {
    AppTheme {
        BudgetCard(
            plannedAmount = "1 200 €",
            spentAmount = "340 €",
            isVisible = true,
            onToggleVisibility = {}
        )
    }
}

@Preview(
    name = "Budget - hidden",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 160
)
@Composable
private fun BudgetCardPreview_Hidden() {
    AppTheme {
        BudgetCard(
            plannedAmount = "1 200 €",
            spentAmount = "340 €",
            isVisible = false,
            onToggleVisibility = {}
        )
    }
}

