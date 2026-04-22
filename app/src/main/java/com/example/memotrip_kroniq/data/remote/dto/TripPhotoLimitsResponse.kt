package com.example.memotrip_kroniq.data.remote.dto

data class TripPhotoLimitsResponse(
    val allowed: Boolean,
    val code: String? = null,
    val plan: String? = null,
    val used: Int? = null,
    val limit: Int? = null
)
