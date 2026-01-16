package com.example.memotrip_kroniq.ui.addtrip.utils

/**
 * Mapuje logický scroll target na index v LazyColumn.
 *
 * ⚠️ Indexy MUSÍ odpovídat pořadí item { } v AddTripContent.
 */
val AddTripScrollIndexMap = mapOf(
    AddTripScrollTarget.TRIP_NAME to 0,
    AddTripScrollTarget.HERO_BANNER to 1,
    AddTripScrollTarget.DESTINATION to 2,
    AddTripScrollTarget.THEME to 3,
    AddTripScrollTarget.DATE to 4,
    AddTripScrollTarget.ROUTE_BLOCK to 5,
    AddTripScrollTarget.TRANSPORT to 6,
    AddTripScrollTarget.CREATE_BUTTON to 7
)
