package com.example.memotrip_kroniq.ui.home

enum class HomePlanTier {
    FREE,
    PREMIUM,
    KRONIQ
}

fun resolveCurrentPlan(
    isPremium: Boolean,
    isKroniq: Boolean
): HomePlanTier = when {
    isKroniq -> HomePlanTier.KRONIQ
    isPremium -> HomePlanTier.PREMIUM
    else -> HomePlanTier.FREE
}
