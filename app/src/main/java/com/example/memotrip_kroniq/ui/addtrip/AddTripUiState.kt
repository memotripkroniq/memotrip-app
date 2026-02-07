package com.example.memotrip_kroniq.ui.addtrip
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.memotrip_kroniq.data.location.LocationSuggestion
import com.example.memotrip_kroniq.ui.addtrip.components.ThemeSelector
import com.example.memotrip_kroniq.ui.core.model.Destination
import com.example.memotrip_kroniq.ui.core.model.ThemeType
import com.example.memotrip_kroniq.ui.core.model.TransportType
import java.time.LocalDate

data class AddTripUiState(

    // 📝 Basic info
    val coverPhotoUri: Uri? = null,
    val coverImageUrl: String? = null,
    val tripName: String = "",

    // Generated map
    val generatedMapImageUrl: String? = null,
    val isGeneratingMap: Boolean = false,
    val isMapDirty: Boolean = false,
    val hasTriedGenerate: Boolean = false,

    // 🌍 Destination
    val destination: Destination? = null,

    // 🎨 Themes
    val selectedTheme: ThemeType? = null,
    val isThemesLocked: Boolean = true,

    // 📅 Date
    val tripStartDate: LocalDate? = null,
    val tripEndDate: LocalDate? = null,

    // 📍 Locations
    val fromLocation: TextFieldValue = TextFieldValue(""),
    val toLocation: TextFieldValue = TextFieldValue(""),
    val isFromFocused: Boolean = false,
    val isToFocused: Boolean = false,
    val focusedStopIndex: Int? = null, // 🆕 WAYPOINT FOCUS (pouze jeden WP může být focused)
    val fromSuggestions: List<LocationSuggestion> = emptyList(),
    val toSuggestions: List<LocationSuggestion> = emptyList(),
    val stops: List<TextFieldValue> = emptyList(),
    val stopSuggestions: List<List<LocationSuggestion>> = emptyList(),
    // 🆕 WAYPOINT VALIDATION
    val showStopErrors: List<Boolean> = emptyList(),

    //val isSearchingFrom: Boolean = false,
    //val isSearchingTo: Boolean = false

    // 🚗 Transport
    val transport: Set<TransportType> = emptySet(),

    // 🔄 UI state
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // ❗ VALIDATION FLAGS (nastavují se až po kliknutí na Create)
    val showTripNameError: Boolean = false,
    val showDestinationError: Boolean = false,
    val showDateError: Boolean = false,
    val showFromLocationError: Boolean = false,
    val showToLocationError: Boolean = false,
    val showTransportError: Boolean = false,
    val showGeneratedMapError: Boolean = false,
    val flowState: AddTripFlowState = AddTripFlowState.IDLE// 🔁 FLOW STATE (saving → success)
)

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun ThemeSelectorPreview() {
    ThemeSelector(
        selected = null,
        locked = true,
        onSelect = {}
    )
}

