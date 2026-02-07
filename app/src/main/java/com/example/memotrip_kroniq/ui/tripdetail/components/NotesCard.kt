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
fun NotesCard(
    items: List<NoteItemUi>,
    onAddClick: () -> Unit = {},
    onRemoveItem: (index: Int) -> Unit = {}
) {
    val ui = LocalUiScaler.current
    val cardColor = Color(0xFF383A41)
    val radius = 12.dp

    Column(
        modifier = Modifier
    ) {
        Text(
            text = "Notes",
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
                // ✅ EMPTY = 1 řádek (bez X)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onAddClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_wp_add),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
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
                // ✅ LIST = položky + X
                items.forEachIndexed { index, item ->
                    NoteItemRow(
                        text = item.text,
                        onRemoveClick = { onRemoveItem(index) }
                    )
                    Spacer(Modifier.height(3f.sy(ui)))
                }

                // ✅ Add button row (jen plusko) – stejně jako checklist
                NotesAddRow(
                    onAddClick = onAddClick
                )
            }
        }
    }
}

@Composable
private fun NotesAddRow(
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
private fun NoteItemRow(
    text: String,
    onRemoveClick: () -> Unit
) {
    val ui = LocalUiScaler.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.tripdetail_ic_note),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = text,
            color = Color.White,
            fontSize = 16f.fs(ui),
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

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
    name = "NotesCard - Empty",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 220
)
@Composable
private fun NotesCardPreview_Empty() {
    AppTheme {
        NotesCard(
            items = emptyList()
        )
    }
}

@Preview(
    name = "NotesCard - With items",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 240
)
@Composable
private fun NotesCardPreview_WithItems() {
    AppTheme {
        NotesCard(
            items = listOf(
                NoteItemUi("Buy groceries before trip"),
                NoteItemUi("Check ferry times")
            )
        )
    }
}
