package com.example.memotrip_kroniq.ui.tripdetail.components

import android.net.Uri
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

@Composable
fun TipsAndTripsCard(
    items: List<TipsAndTripsItemUi>,
    isAdding: Boolean,
    onAddClick: () -> Unit,
    onCancelAddClick: () -> Unit,
    onPickImageClick: () -> Unit,
    onAddItemPhotoClick: (index: Int) -> Unit,
    onRemoveItem: (index: Int) -> Unit
) {
    val ui = LocalUiScaler.current
    val cardColor = Color(0xFF383A41)
    val radius = 12.dp

    Column {
        Row {
            Text(
                text = "Tips & Trips",
                color = Color.White,
                fontSize = 16f.fs(ui),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(10f.sy(ui)))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(radius))
                .background(cardColor, RoundedCornerShape(radius))
                .innerTopShadow(alpha = 0.18f, height = 18f)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            when {
                items.isEmpty() && !isAdding -> {
                    // EMPTY: + Add tips & Trips
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
                            text = "Add tips & Trips",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 16f.fs(ui),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                isAdding -> {
                    TipsTripsAddEditRow(
                        onPickImageClick = onPickImageClick,
                        onCancelClick = onCancelAddClick
                    )

                    Spacer(Modifier.height(10f.sy(ui)))

                    TipsAndTripsSmallAddRow(onAddClick = onAddClick)
                }

                else -> {
                    items.forEachIndexed { index, item ->
                        TipsAndTripsItemRow(
                            title = item.title,
                            imageUri = item.imageUri,
                            onAddPhotoClick = { onAddItemPhotoClick(index) },
                            onRemoveClick = { onRemoveItem(index) }
                        )
                        Spacer(Modifier.height(3f.sy(ui)))
                    }

                    TipsAndTripsSmallAddRow(onAddClick = onAddClick)
                }
            }
        }
    }
}

@Composable
private fun TipsTripsAddEditRow(
    onPickImageClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.tripdetail_ic_point),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2B2E35))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onPickImageClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_wp_add),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = "Add tips & Trips",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 16f.fs(LocalUiScaler.current),
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
                ) { onCancelClick() }
        )
    }
}

@Composable
private fun TipsAndTripsSmallAddRow(
    onAddClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
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
private fun TipsAndTripsItemRow(
    title: String,
    imageUri: Uri?,
    onAddPhotoClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val ui = LocalUiScaler.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.tripdetail_ic_point),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(10.dp))

        // ✅ mezi point a text
        InlinePhotoSlot(
            imageUri = imageUri,
            onAddPhotoClick = onAddPhotoClick
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = title,
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
    name = "TipsTripsCard - Empty",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 220
)
@Composable
private fun TipsTripsCardPreview_Empty() {
    AppTheme {
        TipsAndTripsCard(
            items = emptyList(),
            isAdding = false,
            onAddClick = {},
            onCancelAddClick = {},
            onPickImageClick = {},
            onAddItemPhotoClick = {},
            onRemoveItem = {}
        )
    }
}

@Preview(
    name = "TipsTripsCard - With items",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 240
)
@Composable
private fun TipsTripsCardPreview_WithItems() {
    AppTheme {
        TipsAndTripsCard(
            items = listOf(
                TipsAndTripsItemUi(title = "Hidden beach near the lighthouse", imageUri = null),
                TipsAndTripsItemUi(title = "Try local food market in the center", imageUri = null)
            ),
            isAdding = false,
            onAddClick = {},
            onCancelAddClick = {},
            onPickImageClick = {},
            onAddItemPhotoClick = {},
            onRemoveItem = {}
        )
    }
}
