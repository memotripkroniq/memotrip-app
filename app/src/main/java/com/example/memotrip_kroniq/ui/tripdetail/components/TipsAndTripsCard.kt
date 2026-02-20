package com.example.memotrip_kroniq.ui.tripdetail.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
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

    // ✅ Keep-like inline edit
    editingIndex: Int? = null,
    editingText: TextFieldValue = TextFieldValue(""),
    onStartEdit: (index: Int) -> Unit = {},
    onEditingTextChange: (TextFieldValue) -> Unit = {},
    onCommitEdit: () -> Unit = {},

    onAddClick: () -> Unit,
    onCancelAddClick: () -> Unit,
    onPickImageClick: () -> Unit,                // fotka pro "adding row"
    onAddItemPhotoClick: (index: Int) -> Unit,   // fotka pro konkrétní item
    onRemoveItem: (index: Int) -> Unit
) {
    val ui = LocalUiScaler.current
    val cardColor = Color(0xFF383A41)
    val radius = 12.dp
    val focusManager = LocalFocusManager.current

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
                            ) {
                                focusManager.clearFocus(force = true)
                                onAddClick()
                            },
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

                else -> {
                    // LIST (včetně "adding" řádku, pokud jeAdding=true)
                    items.forEachIndexed { index, item ->
                        TipsAndTripsItemRow(
                            title = item.title,
                            imageUri = item.imageUri,
                            isEditing = (editingIndex == index),
                            editingText = editingText,

                            onStartEdit = {
                                focusManager.clearFocus(force = true)
                                onStartEdit(index)
                            },
                            onEditingTextChange = onEditingTextChange,
                            onCommitEdit = onCommitEdit,

                            onAddPhotoClick = { onAddItemPhotoClick(index) },
                            onRemoveClick = { onRemoveItem(index) }
                        )
                        Spacer(Modifier.height(3f.sy(ui)))
                    }

                    // Small + row
                    TipsAndTripsSmallAddRow(
                        onAddClick = {
                            focusManager.clearFocus(force = true)
                            onAddClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TipsAndTripsSmallAddRow(
    onAddClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.ic_wp_add),
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus(force = true)
                    onAddClick()
                }
        )
    }
}

@Composable
private fun TipsAndTripsItemRow(
    title: String,
    imageUri: Uri?,
    isEditing: Boolean,
    editingText: TextFieldValue,
    onStartEdit: () -> Unit,
    onEditingTextChange: (TextFieldValue) -> Unit,
    onCommitEdit: () -> Unit,
    onAddPhotoClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val ui = LocalUiScaler.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusRequester = remember { FocusRequester() }
    var hadFocus by remember { mutableStateOf(false) }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            hadFocus = false
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            hadFocus = false
        }
    }

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

        InlinePhotoSlot(
            imageUri = imageUri,
            onAddPhotoClick = onAddPhotoClick
        )

        Spacer(Modifier.width(10.dp))

        if (isEditing) {
            BasicTextField(
                value = editingText,
                onValueChange = onEditingTextChange,
                singleLine = true,
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            hadFocus = true
                        } else {
                            if (hadFocus) onCommitEdit()
                        }
                    },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16f.fs(ui),
                    fontWeight = FontWeight.Normal
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onCommitEdit()
                    }
                ),
                decorationBox = { inner ->
                    if (editingText.text.isBlank()) {
                        Text(
                            text = "Tip",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 16f.fs(ui),
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    inner()
                }
            )
        } else {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16f.fs(ui),
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onStartEdit() },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val focusManager = LocalFocusManager.current
        Image(
            painter = painterResource(R.drawable.ic_wp_close),
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus(force = true)
                    onRemoveClick()
                }
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
            editingIndex = 1,
            editingText = TextFieldValue("Try local food market in the center"),
            onStartEdit = {},
            onEditingTextChange = {},
            onCommitEdit = {},
            onAddClick = {},
            onCancelAddClick = {},
            onPickImageClick = {},
            onAddItemPhotoClick = {},
            onRemoveItem = {}
        )
    }
}
