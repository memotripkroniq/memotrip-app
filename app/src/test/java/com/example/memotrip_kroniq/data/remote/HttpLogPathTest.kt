package com.example.memotrip_kroniq.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class HttpLogPathTest {
    @Test fun paymentPathsHideIdentifiersAndSensitiveQueryParameters() {
        assertEquals("/payments/dummy/{purchaseId}/status", redactPaymentLogPath("/payments/dummy/private-id/status?token=private-token"))
        assertEquals("/payments/dummy/{purchaseId}/resolve", redactPaymentLogPath("/payments/dummy/private-id/resolve"))
    }
    @Test fun standardEndpointPathsRemainReadable() {
        assertEquals("/auth/me", redactPaymentLogPath("/auth/me"))
        assertEquals("/payments/dummy", redactPaymentLogPath("/payments/dummy"))
    }
}
