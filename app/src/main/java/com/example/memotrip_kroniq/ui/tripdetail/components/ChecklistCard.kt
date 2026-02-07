package com.example.memotrip_kroniq.ui.tripdetail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerTopShadow
import com.example.memotrip_kroniq.ui.theme.AppTheme
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme

@Composable
fun ChecklistCard(
    items: List<ChecklistItemUi>,
    onAddClick: () -> Unit = {},
    onToggleChecked: (index: Int) -> Unit = {},
    onRemoveItem: (index: Int) -> Unit = {}
) {
    val ui = LocalUiScaler.current
    val cardColor = Color(0xFF383A41)
    val radius = 12.dp

    Column(
        modifier = Modifier
    ) {
        Text(
            text = "Checklist",
            color = Color.White,
            fontSize = 16f.fs(ui),
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10f.sy(ui)))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(radius))
                .background(cardColor, RoundedCornerShape(radius))
                .innerTopShadow(alpha = 0.18f, height = 18f)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {

            if (items.isEmpty()) {
                // EMPTY STATE: + Add notes (NO X)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_wp_add),
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onAddClick() }
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "Add notes",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 16f.fs(ui),
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                // LIST STATE: render each item row with X
                items.forEachIndexed { index, item ->
                    ChecklistItemRow(
                        item = item,
                        onToggleChecked = { onToggleChecked(index) },
                        onRemoveClick = { onRemoveItem(index) }
                    )

                    Spacer(Modifier.height(6f.sy(ui)))
                }

                //Spacer(Modifier.height(8f.sy(ui)))

                // Add button row under the list (as in Figma)
                ChecklistAddRow(
                    onAddClick = onAddClick
                )
            }

        }
    }
}

@Composable
private fun ChecklistAddRow(
    onAddClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_wp_add),
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onAddClick() }
        )
    }
}


@Composable
private fun ChecklistItemRow(
    item: ChecklistItemUi,
    onToggleChecked: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val ui = LocalUiScaler.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // LEFT: checkbox / check icon
        Image(
            painter = painterResource(
                if (item.checked)
                    R.drawable.tripdetail_ic_checkbox_checked   // ✓ (dodáš / nebo použij existující)
                else
                    R.drawable.tripdetail_ic_checkbox_unchecked     // prázdný čtverec
            ),
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onToggleChecked() }
        )

        Spacer(Modifier.width(10.dp))

        // TEXT
        Text(
            text = item.text,
            fontSize = 16f.fs(ui),
            fontWeight = FontWeight.Normal,
            color =
                if (item.checked)
                    Color.White.copy(alpha = 0.45f)
                else
                    Color.White,
            textDecoration =
                if (item.checked)
                    TextDecoration.LineThrough
                else
                    TextDecoration.None,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // REMOVE
        Image(
            painter = painterResource(R.drawable.ic_wp_close),
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onRemoveClick() }
        )
    }
}



@Preview(
    name = "ChecklistCard - Empty",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 220
)
@Composable
private fun ChecklistCardPreview_Empty() {
    AppTheme {
        ChecklistCard(
            items = emptyList(),
            onAddClick = {},
            onRemoveItem = {}
        )
    }
}

@Preview(
    name = "Checklist - mixed",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 260
)
@Composable
private fun ChecklistCardPreview_Checked() {
    AppTheme {
        ChecklistCard(
            items = listOf(
                ChecklistItemUi("Passport", checked = true),
                ChecklistItemUi("Powerbank", checked = false),
                ChecklistItemUi("Sunscreen", checked = false)
            ),
            onAddClick = {},
            onToggleChecked = {},
            onRemoveItem = {}
        )
    }
}


