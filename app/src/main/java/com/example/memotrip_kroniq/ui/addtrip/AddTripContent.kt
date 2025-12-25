package com.example.memotrip_kroniq.ui.addtrip

import PreviewUiScaler
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.memotrip_kroniq.ui.addtrip.components.AddTripHeroBanner
import com.example.memotrip_kroniq.ui.addtrip.components.AddTripNameField
import com.example.memotrip_kroniq.ui.addtrip.components.DateField
import com.example.memotrip_kroniq.ui.addtrip.components.DestinationSelector
import com.example.memotrip_kroniq.ui.addtrip.components.LocationField
import com.example.memotrip_kroniq.ui.addtrip.components.PrimaryNextButton
import com.example.memotrip_kroniq.ui.addtrip.components.ThemeSelector
import com.example.memotrip_kroniq.ui.addtrip.components.TransportSelector
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.time.LocalDate
import com.example.memotrip_kroniq.ui.addtrip.DateRange
import com.example.memotrip_kroniq.ui.addtrip.components.AddTripDatePickerOverlay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTripContent(
    modifier: Modifier = Modifier,
    uiState: AddTripUiState,
    onTripNameChange: (String) -> Unit,
    onDestinationSelected: (Destination) -> Unit,
    onThemeSelected: (ThemeType) -> Unit,
    onDateSelected: (DateRange) -> Unit,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onTransportSelected: (TransportType) -> Unit,
    onNextClick: () -> Unit
) {
    val s = LocalUiScaler.current

    Column(
        modifier = modifier              // 🔥 TADY SE TO LÁME
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState()),
        //horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* ✍️ Trip name */
        AddTripNameField(
            value = uiState.tripName,
            onValueChange = onTripNameChange
        )

        Spacer(modifier = Modifier.height(16f.sy(s)))

        /* 🌍 Hero banner */
        AddTripHeroBanner()

        Spacer(modifier = Modifier.height(16f.sy(s)))

        /* 🌍 Destination */
        DestinationSelector(
            selected = uiState.destination,
            onSelect = onDestinationSelected
        )

        Spacer(modifier = Modifier.height(20f.sy(s)))

        /* 🎨 Theme */
        ThemeSelector(
            selected = uiState.selectedTheme,
            locked = uiState.isThemesLocked,
            onSelect = onThemeSelected
        )


        Spacer(modifier = Modifier.height(20f.sy(s)))

        /* 📅 Date */
        DateField(
            startDate = uiState.tripStartDate,
            endDate = uiState.tripEndDate,
            showError = uiState.showDateError,
            onClick = onFromClick
        )

        Spacer(modifier = Modifier.height(12f.sy(s)))

        /* 📍 From / To */
        LocationField(
            label = "From",
            value = uiState.fromLocation,
            onClick = onFromClick
        )

        Spacer(modifier = Modifier.height(12f.sy(s)))

        LocationField(
            label = "To",
            value = uiState.toLocation,
            onClick = onToClick
        )

        Spacer(modifier = Modifier.height(20f.sy(s)))

        /* 🚗 Transport */
        TransportSelector(
            selected = uiState.transport,
            onSelect = onTransportSelected
        )

        Spacer(modifier = Modifier.height(28f.sy(s)))

        /* ▶️ Next */
        PrimaryButton(
            text = "Next",
            enabled = true,
            onClick = onNextClick,
            modifier = Modifier
                .width(200f.sx(s))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24f.sy(s)))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 412, heightDp = 1090)
@Composable
fun AddTripContentPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            AddTripContent(
                modifier = Modifier,
                uiState = AddTripUiState(
                    tripName = "",
                    destination = Destination.EUROPE,
                    selectedTheme = ThemeType.SUMMER,
                    isThemesLocked = false,
                    tripStartDate = null,
                    tripEndDate = null,
                    //tripStartDate = LocalDate.of(2025, 6, 28),
                    //tripEndDate = LocalDate.of(2025, 7, 11),
                    fromLocation = "",
                    toLocation = "",
                    transport = TransportType.CARAVAN
                ),
                onTripNameChange = {},
                onDestinationSelected = {},
                onThemeSelected = {},
                onDateSelected = {}, // ✅ OPRAVENO
                onFromClick = {},
                onToClick = {},
                onTransportSelected = {},
                onNextClick = {}
            )
        }
    }
}

