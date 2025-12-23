package com.example.memotrip_kroniq.ui.addtrip

import java.time.LocalDate

data class DateRange(
    val start: LocalDate,
    val end: LocalDate
) {
    init {
        require(!end.isBefore(start)) { "end must be >= start" }
    }
}
