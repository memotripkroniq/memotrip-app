package com.example.memotrip_kroniq.ui.addtrip.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.data.location.LocationSuggestion
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

// ⭐ NOVÉ – prostor pro křížek vpravo
private val RemoveIconSpace = 36.dp


@Composable
fun WaypointField(
    index: Int,
    value: TextFieldValue,
    suggestions: List<LocationSuggestion>,
    error: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onSuggestionSelected: (LocationSuggestion) -> Unit,
    onRemoveClick: () -> Unit,
    focusRequester: FocusRequester? = null
) {

    //--------------------
    //------UI------------
    //--------------------

    Column {

        // ⬇️ OVERLAY KONTEJNER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier
                )
        ) {

            // INPUT (beze změny)
            LocationField(
                label = "Stop ${index + 1}",
                value = value,
                onValueChange = {
                    Log.d("WP_UI", "Typing '${it.text}'")
                    onValueChange(it)
                },
                onFocusChange = onFocusChange,
                error = error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = RemoveIconSpace)
            )

            // ❌ KŘÍŽEK UVNITŘ INPUTU
            Icon(
                painter = painterResource(id = R.drawable.ic_wp_close),
                contentDescription = "Remove stop",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(y = 10.dp)
                    .padding(end = 12.dp)   // ⬅️ odsazení nejdřív
                    .size(15.dp)           // ⬅️ skutečná velikost ikony
                    .clickable { onRemoveClick() }
            )
        }
    }

}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun WaypointFieldPreview() {
    MemoTripTheme {

        val textState = remember {
            mutableStateOf(TextFieldValue("Vienna, Austria"))
        }

        WaypointField(
            index = 0,
            value = textState.value,
            suggestions = listOf(
                LocationSuggestion(
                    displayName = "Vienna, Austria",
                    lat = 48.2082,
                    lon = 16.3738
                ),
                LocationSuggestion(
                    displayName = "Brno, Czech Republic",
                    lat = 49.1951,
                    lon = 16.6068
                )
            ),
            error = false,
            onValueChange = { textState.value = it },
            onFocusChange = {},
            onSuggestionSelected = {},
            onRemoveClick = {},
        )
    }
}
