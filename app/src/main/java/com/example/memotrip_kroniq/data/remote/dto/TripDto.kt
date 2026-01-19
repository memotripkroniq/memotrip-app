package com.example.memotrip_kroniq.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TripDto(
    val id: String,
    val title: String,
    val coverImageUrl: String? = null
)
