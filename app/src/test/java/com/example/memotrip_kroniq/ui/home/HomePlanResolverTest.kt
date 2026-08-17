package com.example.memotrip_kroniq.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomePlanResolverTest {

    @Test
    fun falseFalse_resolvesFreeAsActive() {
        assertEquals(HomePlanTier.FREE, resolveCurrentPlan(isPremium = false, isKroniq = false))
    }

    @Test
    fun trueFalse_resolvesPremiumAsActive() {
        assertEquals(HomePlanTier.PREMIUM, resolveCurrentPlan(isPremium = true, isKroniq = false))
    }

    @Test
    fun falseTrue_resolvesKroniqAsActive() {
        assertEquals(HomePlanTier.KRONIQ, resolveCurrentPlan(isPremium = false, isKroniq = true))
    }
}
