package com.example.memotrip_kroniq.ui.locationsearch

import PreviewUiScaler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.memotrip_kroniq.data.location.LocationSuggestion
import com.example.memotrip_kroniq.navigation.*
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.memotrip_kroniq.ui.home.components.modifiers.innerShadow
import com.example.memotrip_kroniq.ui.core.*
import androidx.compose.runtime.collectAsState



@Composable
fun FullScreenLocationSearchScreen(
    navController: NavHostController,
    viewModel: LocationSearchViewModel,
    modifier: Modifier = Modifier
) {
    val suggestions by viewModel.suggestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val s = LocalUiScaler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // 🔹 TOP BAR – full width (správně)
        AppTopBar(
            title = "Search location",
            showBack = true,
            onBackClick = { navController.popBackStack() }
        )

        // 🔹 CONTENT WRAPPER – JEDNOTNÉ ODSAZENÍ JAKO AddTripScreen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16f.sx(s))
        ) {

            Spacer(Modifier.height(12.dp))

            SearchInput(
                onQueryChange = viewModel::onQueryChange
            )

            Spacer(Modifier.height(8.dp))

            val query by viewModel.query.collectAsState()
            when {
                query.length < 4 -> {
                    // ⛔ nic nezobrazujeme
                }

                isLoading -> {
                    InfoRow("Searching…")
                }

                suggestions.isEmpty() -> {
                    InfoRow("No results")
                }

                else -> {
                    LazyColumn {
                        items(suggestions) { suggestion ->
                            LocationSearchRow(
                                suggestion = suggestion,
                                onClick = {
                                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                                        set(LOCATION_NAME_KEY, suggestion.displayName)
                                        set(LOCATION_LAT_KEY, suggestion.lat)
                                        set(LOCATION_LON_KEY, suggestion.lon)
                                    }

                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchInput(
    onQueryChange: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val s = LocalUiScaler.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(45f.sy(s))
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF353D4E))
            //.border(
            //    width = 1.5.dp,
            //    color = Color(0xFF747781),
            //    shape = RoundedCornerShape(10.dp)
            //)
            //.innerShadow(
            //    color = Color.White,
            //    offsetX = 0f,
            //    offsetY = 1f,
            //    blur = 2f,
            //    cornerRadius = 10f
            //)
            .padding(horizontal = 16f.sx(s)),
        contentAlignment = Alignment.CenterStart
    ) {

        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onQueryChange(it)
            },
            singleLine = true,
            cursorBrush = SolidColor(Color.White), // ✅ bílý kurzor
            textStyle = LocalTextStyle.current.copy(
                color = Color.White,
                fontSize = 16f.fs(s)
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isBlank()) {
                        Text(
                            text = "Add location",
                            color = Color(0xFFB5BEC7),
                            fontSize = 16f.fs(s)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}



@Composable
private fun LocationSearchRow(
    suggestion: LocationSuggestion,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp) // ✅ bez horizontal
    ) {
        Text(
            text = suggestion.displayName,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}


@Composable
private fun InfoRow(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 412,
    heightDp = 892
)
@Composable
private fun FullScreenLocationSearchScreenPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        FakeFullScreenLocationSearchScreen()
    }
}

@Composable
private fun FakeFullScreenLocationSearchScreen() {
    val s = LocalUiScaler.current

    val fakeSuggestions = listOf(
        LocationSuggestion("Rome, Italy", 41.9, 12.5),
        LocationSuggestion("Roma Termini", 41.9, 12.5),
        LocationSuggestion("Rome Airport (FCO)", 41.8, 12.2)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // TOP BAR = full width (stejně jako v appce)
        AppTopBar(
            title = "Search location",
            showBack = true,
            onBackClick = {}
        )

        // ✅ WRAPPER s paddingem stejně jako AddTripScreen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16f.sx(s))
        ) {
            Spacer(Modifier.height(12.dp))

            SearchInput(onQueryChange = {})

            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(fakeSuggestions) { suggestion ->
                    LocationSearchRow(
                        suggestion = suggestion,
                        onClick = {}
                    )
                }
            }
        }
    }
}
