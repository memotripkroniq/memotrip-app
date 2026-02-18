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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.fs
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.theme.AppTheme

enum class BudgetEditField { PLANNED, SPENT }

@Composable
fun BudgetCard(
    plannedAmount: String,
    spentAmount: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,

    // ✅ Keep-like inline edit
    editingField: BudgetEditField? = null,
    editingText: TextFieldValue = TextFieldValue(""),
    onStartEdit: (BudgetEditField) -> Unit = {},
    onEditingTextChange: (TextFieldValue) -> Unit = {},
    onCommitEdit: () -> Unit = {}
) {
    val ui = LocalUiScaler.current
    val cardColor = Color(0xFF383A41)
    val radius = 10.dp
    val focusManager = LocalFocusManager.current

    Column {
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

            // Planned = levý sloupec (zabere půlku řádku)
            Box(modifier = Modifier.weight(1f)) {
                BudgetInlineRow(
                    label = "Planned:",
                    value = plannedAmount,
                    isVisible = isVisible,
                    isEditing = (editingField == BudgetEditField.PLANNED),
                    editingText = editingText,
                    onClick = {
                        if (!isVisible) return@BudgetInlineRow
                        focusManager.clearFocus(force = true)
                        onStartEdit(BudgetEditField.PLANNED)
                    },
                    onEditingTextChange = onEditingTextChange,
                    onCommitEdit = onCommitEdit
                )
            }

            Spacer(Modifier.width(12.dp)) // malá mezera mezi sloupci

            // Spent = pravý sloupec (zabere půlku řádku)
            Box(modifier = Modifier.weight(1f)) {
                BudgetInlineRow(
                    label = "Spent:",
                    value = spentAmount,
                    isVisible = isVisible,
                    isEditing = (editingField == BudgetEditField.SPENT),
                    editingText = editingText,
                    onClick = {
                        if (!isVisible) return@BudgetInlineRow
                        focusManager.clearFocus(force = true)
                        onStartEdit(BudgetEditField.SPENT)
                    },
                    onEditingTextChange = onEditingTextChange,
                    onCommitEdit = onCommitEdit
                )
            }

            // Eye = vždy viditelné, fixní na konci
            val focusManager = LocalFocusManager.current
            Image(
                painter = painterResource(if (isVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(22.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus(force = true)
                        onToggleVisibility()
                    }
            )

        }

    }
}

@Composable
private fun BudgetInlineRow(
    label: String,
    value: String,
    isVisible: Boolean,
    isEditing: Boolean,
    editingText: TextFieldValue,
    onClick: () -> Unit,
    onEditingTextChange: (TextFieldValue) -> Unit,
    onCommitEdit: () -> Unit
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
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 16f.fs(ui)
        )

        Spacer(Modifier.width(12.dp))

        if (!isVisible) {
            Text(
                text = "******",
                color = Color.White,
                fontSize = 16f.fs(ui),
                fontWeight = FontWeight.SemiBold
            )
            return
        }

        if (isEditing) {
            BasicTextField(
                value = editingText,
                onValueChange = onEditingTextChange,
                singleLine = true,
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .widthIn(min = 40.dp, max = 120.dp)
                    .padding(end = 8.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { st ->
                        if (st.isFocused) {
                            hadFocus = true
                        } else {
                            if (hadFocus) onCommitEdit()
                        }
                    },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16f.fs(ui),
                    fontWeight = FontWeight.SemiBold
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text
                ),
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
                            text = "Amount",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 16f.fs(ui),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    inner()
                }
            )
        } else {
            Text(
                text = value,
                color = Color.White,
                fontSize = 16f.fs(ui),
                fontWeight = FontWeight.SemiBold
            )
        }
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
            onToggleVisibility = {},
            editingField = BudgetEditField.PLANNED,
            editingText = TextFieldValue("1200")
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
