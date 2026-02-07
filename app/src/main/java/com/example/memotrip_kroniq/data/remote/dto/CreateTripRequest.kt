package com.example.memotrip_kroniq.data.remote.dto

data class CreateTripRequest(
    val name: String,
    val destination: String,      // "EUROPE" ...
    val dateFrom: String,         // "2026-07-01"
    val dateTo: String,           // "2026-07-14"
    val from: String,
    val to: String,
    val transport: String,        // "CAR" ...
    val waypoints: List<String> = emptyList(),
    val theme: String? = null,
    val coverImageUrl: String? = null,
    val mapImageUrl: String
)