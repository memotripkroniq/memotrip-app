package com.example.memotrip_kroniq.data.payments

import org.junit.Assert.assertEquals
import org.junit.Test

class DummyPaymentPlanTest {

    @Test
    fun premiumPlanMapsToPremiumRequest() {
        assertEquals("PREMIUM", DummyPaymentPlan.PREMIUM.toRequest().plan)
    }

    @Test
    fun kroniqPlanMapsToKroniqRequest() {
        assertEquals("KRONIQ", DummyPaymentPlan.KRONIQ.toRequest().plan)
    }
}
