package com.example.memotrip_kroniq.data.payments

import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentRequest

enum class DummyPaymentPlan(val apiValue: String) {
    PREMIUM("PREMIUM"),
    KRONIQ("KRONIQ");

    fun toRequest(): DummyPaymentRequest = DummyPaymentRequest(plan = apiValue)
}
