package com.example.memotrip_kroniq.data.tripmap

import kotlinx.serialization.Serializable

@Serializable
data class GenerateTripMapRequest(
    val from: String,
    val stops: List<String> = emptyList(),
    val to: String,
    val transports: List<String>
)

@Serializable
data class GenerateTripMapResponse(
    val imageUrl: String
)
