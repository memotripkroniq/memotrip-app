package com.example.memotrip_kroniq.navigation

import org.junit.Assert.*
import org.junit.Test

class ResetPasswordLinkParserTest {
    private val token = "123e4567-e89b-42d3-a456-426614174000"
    private val base = "https://memotrip-bff-development.up.railway.app/auth/reset-password/open"

    @Test fun acceptsExactHttpsAndLegacyLinks() {
        assertEquals(token, ResetPasswordLinkParser.parse("$base#token=$token"))
        assertEquals(token, ResetPasswordLinkParser.parse("memotrip://reset-password?token=$token"))
        assertEquals(token, ResetPasswordLinkParser.parse("$base#token=${token.uppercase()}"))
    }

    @Test fun rejectsAmbiguousAndMalformedLinks() {
        val invalid = listOf(
            null, "", base, "$base#token=", "$base#token=not-a-uuid",
            "$base#token=1-1-1-1-1", "$base#token=$token&token=$token",
            "$base#token=$token&other=1", "$base#other=1&token=$token",
            "$base?token=$token", "$base?#token=$token", "$base?other=1#token=$token",
            "$base/#token=$token", "$base/extra#token=$token",
            "${base.replace("https:", "http:")}#token=$token",
            "${base.replace("development", "staging")}#token=$token",
            "${base.replace(".app/", ".app.evil.example/")}#token=$token",
            "${base.replace("https://", "https://user@")}#token=$token",
            "${base.replace(".app/", ".app:443/")}#token=$token",
            "${base.replace("/open", "/%6fpen")}#token=$token",
            "$base#token=%31${token.drop(1)}", "$base#token=$token%0A",
            "memotrip://reset-password#token=$token",
            "memotrip://reset-password?token=$token&token=$token",
            "memotrip://other?token=$token", "$base#token=$token#extra"
        )
        invalid.forEach { assertNull("Unexpected accepted input", ResetPasswordLinkParser.parse(it)) }
    }

    @Test fun onlyDevelopmentDebugEnablesNewFlow() {
        for (flavor in listOf("development", "staging", "production")) {
            for (build in listOf("debug", "release")) {
                assertEquals(flavor == "development" && build == "debug",
                    ResetPasswordLinkParser.enabled(flavor, build))
            }
        }
    }
}
