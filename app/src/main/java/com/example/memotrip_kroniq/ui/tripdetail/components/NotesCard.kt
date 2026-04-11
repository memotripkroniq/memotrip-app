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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerTopShadow
import com.example.memotrip_kroniq.ui.theme.AppTheme
import androidx.compose.ui.text.input.TextFieldValue


@Composable
fun NotesCard(
    items: List<NoteItemUi>,
    onAddClick: () -> Unit = {},
    onRemoveItem: (index: Int) -> Unit = {},

    // ✅ Keep-like edit support
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
    val focusManager = LocalFocusManager.current

    Column {
        Text(
            text = stringResource(R.string.trip_detail_notes),
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
                // EMPTY STATE: řádek "Add notes"
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
                        text = stringResource(R.string.trip_detail_add_notes),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 16f.fs(ui),
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                // LIST STATE
                items.forEachIndexed { index, item ->
                    NoteItemRow(
                        text = item.text,
                        isEditing = (editingIndex == index),
                        editingText = editingText,
                        onStartEdit = { onStartEdit(index) },
                        onEditingTextChange = onEditingTextChange,
                        onCommitEdit = onCommitEdit,
                        onCancelEdit = onCancelEdit,
                        onRemoveClick = { onRemoveItem(index) }
                    )
                    Spacer(Modifier.height(3f.sy(ui)))
                }

                NotesAddRow(
                    onAddClick = {
                        focusManager.clearFocus(force = true)
                        onAddClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun NotesAddRow(
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
private fun NoteItemRow(
    text: String,
    isEditing: Boolean,
    editingText: TextFieldValue,
    onStartEdit: () -> Unit,
    onEditingTextChange: (TextFieldValue) -> Unit,
    onCommitEdit: () -> Unit,
    onCancelEdit: () -> Unit,
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
            painter = painterResource(R.drawable.tripdetail_ic_note),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
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
                            text = stringResource(R.string.trip_detail_note_placeholder),
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
                text = text,
                color = Color.White,
                fontSize = 16f.fs(ui),
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        // ✅ ukonči editaci aktuální poznámky (spustí commit přes onFocusChanged)
                        focusManager.clearFocus(force = true)

                        // ✅ pak přepni editaci na tuto poznámku
                        onStartEdit()
                    },
                overflow = TextOverflow.Ellipsis
            )
        }


        Image(
            painter = painterResource(R.drawable.ic_wp_close),
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (isEditing) onCancelEdit()
                    onRemoveClick()
                }
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
        NotesCard(items = emptyList())
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
            ),
            editingIndex = 0,
            editingText = TextFieldValue("Buy groceries before trip")
        )
    }
}
