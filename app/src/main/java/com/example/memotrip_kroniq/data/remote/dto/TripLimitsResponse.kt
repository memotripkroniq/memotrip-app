package com.example.memotrip_kroniq.data.remote.dto

data class TripLimitsResponse(
    val allowed: Boolean,
    val code: String? = null,
    val plan: String? = null,
    val used: Int? = null,
    val limit: Int? = null,
    val windowDays: Int? = null,
    val windowStart: String? = null
)
