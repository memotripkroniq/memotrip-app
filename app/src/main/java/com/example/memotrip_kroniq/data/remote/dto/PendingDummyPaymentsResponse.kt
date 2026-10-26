package com.example.memotrip_kroniq.data.remote.dto

// Recovery never contains a payment URL or its token.
data class PendingDummyPaymentItem(
    val purchaseId: String,
    val provider: String,
    val status: String,
    val plan: String,
    val createdAt: String
)

data class PendingDummyPaymentsResponse(
    val items: List<PendingDummyPaymentItem>,
    val nextCursor: String?
)
