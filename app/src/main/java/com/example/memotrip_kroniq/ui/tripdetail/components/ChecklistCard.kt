package com.example.memotrip_kroniq.ui.tripdetail.components

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun ChecklistCard(
    items: List<ChecklistItemUi>,
    onAddClick: () -> Unit = {},
    onToggleChecked: (index: Int) -> Unit = {},
    onRemoveItem: (index: Int) -> Unit = {},

    // ✅ inline edit (bez viditelného inputu)
    editingIndex: Int? = null,
    editingText: TextFieldValue = TextFieldValue(""),
    onStartEdit: (index: Int) -> Unit = {},
    onEditingTextChange: (TextFieldValue) -> Unit = {},
    onCommitEdit: () -> Unit = {},
    onCancelEdit: () -> Unit = {}
) {
    val ui = LocalUiScaler.current
    val cardColor = Color(0xFF383A41)
    val radius = 12.dp

    Column {
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

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "Add notes",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 16f.fs(ui),
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                items.forEachIndexed { index, item ->
                    ChecklistItemRow(
                        item = item,
                        isEditing = (editingIndex == index),
                        editingText = editingText,
                        onToggleChecked = { onToggleChecked(index) },
                        onRemoveClick = { onRemoveItem(index) },
                        onStartEdit = { onStartEdit(index) },
                        onEditingTextChange = onEditingTextChange,
                        onCommitEdit = onCommitEdit,
                        onCancelEdit = onCancelEdit
                    )

                    Spacer(Modifier.height(6f.sy(ui)))
                }

                ChecklistAddRow(onAddClick = onAddClick)
            }
        }
    }
}

@Composable
private fun ChecklistAddRow(
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
                    // 1) ukonči editaci (spustí commit přes onFocusChanged)
                    focusManager.clearFocus(force = true)

                    // 2) přidej nový item až po uvolnění focusu
                    onAddClick()
                }
        )
    }
}


@Composable
private fun ChecklistItemRow(
    item: ChecklistItemUi,
    isEditing: Boolean,
    editingText: TextFieldValue,
    onToggleChecked: () -> Unit,
    onRemoveClick: () -> Unit,
    onStartEdit: () -> Unit,
    onEditingTextChange: (TextFieldValue) -> Unit,
    onCommitEdit: () -> Unit,
    onCancelEdit: () -> Unit
) {
    val ui = LocalUiScaler.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val textColor =
        if (item.checked) Color.White.copy(alpha = 0.45f) else Color.White
    val decoration =
        if (item.checked) TextDecoration.LineThrough else TextDecoration.None

    // ✅ pro Keep-like autofocus
    val focusRequester = remember { FocusRequester() }

    // ✅ guard: necommituj hned při prvním renderu (když ještě nemá focus)
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
            painter = painterResource(
                if (item.checked)
                    R.drawable.tripdetail_ic_checkbox_checked
                else
                    R.drawable.tripdetail_ic_checkbox_unchecked
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

        if (isEditing) {
            BasicTextField(
                value = editingText,
                onValueChange = onEditingTextChange,
                singleLine = true,
                cursorBrush = SolidColor(Color.White), // ✅ bílý kurzor
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            hadFocus = true
                        } else {
                            // ✅ commit až když už to focus někdy mělo
                            if (hadFocus) onCommitEdit()
                        }
                    },
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 16f.fs(ui),
                    fontWeight = FontWeight.Normal,
                    textDecoration = decoration
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onCommitEdit()
                    }
                ),
                decorationBox = { innerTextField ->
                    if (editingText.text.isBlank()) {
                        Text(
                            text = "List item",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 16f.fs(ui),
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
            )
        } else {
            Text(
                text = item.text,
                fontSize = 16f.fs(ui),
                fontWeight = FontWeight.Normal,
                color = textColor,
                textDecoration = decoration,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        // ✅ nejdřív ukonči editaci aktuálního řádku (spustí commit přes onFocusChanged)
                        focusManager.clearFocus(force = true)

                        // ✅ až potom přepni editaci na tento item
                        onStartEdit()
                    },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Image(
            painter = painterResource(R.drawable.ic_wp_close),
            contentDescription = null,
            modifier = Modifier
                .size(15.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (isEditing) {
                        keyboardController?.hide()
                        onCancelEdit()
                    }
                    onRemoveClick()
                }
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
            editingIndex = 1,
            editingText = TextFieldValue("Powerbank")

        )
    }
}
