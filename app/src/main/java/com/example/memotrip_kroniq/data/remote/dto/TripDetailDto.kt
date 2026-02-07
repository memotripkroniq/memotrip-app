package com.example.memotrip_kroniq.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TripDetailDto(
    val id: String,
    val name: String,
    val destination: String? = null,
    val transport: String,
    val from: String,
    val to: String,
    val waypoints: List<String> = emptyList(),
    val startDate: String,
    val endDate: String,
    val theme: String? = null,
    val coverImageUrl: String? = null,
    val mapImageUrl: String? = null,
)
