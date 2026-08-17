package com.example.memotrip_kroniq.data.remote.dto

data class DummyPaymentResponse(
    val purchaseId: String,
    val provider: String,
    val status: String,
    val plan: String,
    val amount: String,
    val currency: String,
    val paymentUrl: String
)
