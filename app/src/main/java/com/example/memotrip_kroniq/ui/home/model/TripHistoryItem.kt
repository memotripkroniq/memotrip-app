package com.example.memotrip_kroniq.ui.home.model

data class TripHistoryItem(
    val id: String,
    val title: String,
    val coverImageUrl: String?,
    val theme: String?,
    val isSharedInKroniq: Boolean = false
)
