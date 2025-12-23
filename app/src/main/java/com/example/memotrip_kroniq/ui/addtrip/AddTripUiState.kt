package com.example.memotrip_kroniq.ui.addtrip
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.addtrip.components.ThemeSelector
import java.time.LocalDate

enum class Destination(
    val label: String,
    val iconRes: Int
) {
    EUROPE("Europe", R.drawable.ic_destination_europe),
    ASIA("Asia", R.drawable.ic_destination_asia),
    AFRICA("Africa", R.drawable.ic_destination_africa),
    NORTH_AMERICA("North America", R.drawable.ic_destination_north_america),
    SOUTH_AMERICA("South America", R.drawable.ic_destination_south_america),
    AUSTRALIA("Australia", R.drawable.ic_destination_australia)
}

enum class TransportType(
    val label: String,
    val iconRes: Int
) {
    PLANE("Plane", R.drawable.ic_transport_plane),
    CAR("Car", R.drawable.ic_transport_car),
    CAMPER("Camper", R.drawable.ic_transport_camper),
    CARAVAN("Caravan", R.drawable.ic_transport_caravan),
    MOTORCYCLE("Motorcycle", R.drawable.ic_transport_motorcycle),
    BIKE("Bike", R.drawable.ic_transport_bike)
}

enum class ThemeType(
    val label: String,
    val imageRes: Int
) {
    SUMMER("Summer", R.drawable.homescreen_theme_summer),
    WINTER("Winter", R.drawable.homescreen_theme_winter),
    CAMPING("Camping", R.drawable.homescreen_theme_camping),
    CITIES("Cities", R.drawable.homescreen_theme_cities),
    NATURE("Nature", R.drawable.homescreen_theme_nature),
    EXOTIC("Exotic", R.drawable.homescreen_theme_exotic)
}


data class AddTripUiState(

    // 📝 Basic info
    val tripName: String = "",

    // 🌍 Destination
    val destination: Destination? = null,

    // 🎨 Themes
    val selectedTheme: ThemeType? = null,
    val isThemesLocked: Boolean = true,

    // 📅 Date
    val tripStartDate: LocalDate? = null,
    val tripEndDate: LocalDate? = null,

    // 📍 Locations
    val fromLocation: String = "",
    val toLocation: String = "",
    val showDateError: Boolean = false,

    // 🚗 Transport
    val transport: TransportType? = null,

    // 🔄 UI state
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // ❗ VALIDATION FLAGS (nastavují se až po kliknutí na Next)
    val showTripNameError: Boolean = false,
    val showDestinationError: Boolean = false,
    val showFromLocationError: Boolean = false,
    val showToLocationError: Boolean = false,
    val showTransportError: Boolean = false
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

