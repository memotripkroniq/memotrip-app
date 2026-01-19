package com.example.memotrip_kroniq.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateTripResponse(
    val id: String,
    val name: String,
    val createdAt: String
)
