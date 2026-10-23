package com.example.memotrip_kroniq.ui.home.payments

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DummyPayLauncherTest {
    private val backend = "https://memotrip-bff-development.up.railway.app/"

    @Test fun configuredHttpsOriginIsAllowed() {
        assertTrue(isAllowedDummyPayUrl("${backend}payments/dummy/id?token=opaque", backend))
        assertTrue(isAllowedDummyPayUrl("${backend.removeSuffix("/")}:443/pay", backend))
    }

    @Test fun unsafeOrDifferentOriginsAreRejected() {
        listOf(
            backend.replace("https:", "http:"),
            "javascript:alert(1)", "intent://pay", "file:///pay", "not a url",
            "https://memotrip-bff-production.up.railway.app/pay",
            "https://memotrip-bff-development.up.railway.app.attacker.example/pay",
            "https://memotrip-bff-development.up.railway.app@attacker.example/pay",
            "https://user@memotrip-bff-development.up.railway.app/pay",
            "https://memotrip-bff-development.up.railway.app:444/pay",
            "${backend}pay#fragment"
        ).forEach { assertFalse(it, isAllowedDummyPayUrl(it, backend)) }
    }
}
