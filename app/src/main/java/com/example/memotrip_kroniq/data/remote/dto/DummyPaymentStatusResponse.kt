package com.example.memotrip_kroniq.data.remote.dto

// Strings deliberately preserve unknown server values for explicit validation in the flow.
data class DummyPaymentStatusResponse(
    val purchaseId: String,
    val provider: String,
    val status: String,
    val plan: String
)
