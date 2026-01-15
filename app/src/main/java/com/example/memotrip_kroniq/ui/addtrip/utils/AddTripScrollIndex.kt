package com.example.memotrip_kroniq.ui.addtrip.utils

/**
 * Mapuje položky LazyColumnu na indexy
 * ➜ když přidáš / odebereš item, upravíš jen tady
 */
object AddTripScrollIndex {

    const val TRIP_NAME = 0
    const val HERO = 1
    const val DESTINATION = 2
    const val THEME = 3
    const val DATE = 4
    const val FROM = 5
    const val ADD_STOP_BUTTON = 6

    fun firstStop(stopIndex: Int): Int =
        ADD_STOP_BUTTON + 1 + stopIndex

    fun to(stopsCount: Int): Int =
        ADD_STOP_BUTTON + 1 + stopsCount

    fun transport(stopsCount: Int): Int =
        ADD_STOP_BUTTON + 2 + stopsCount
}
