package com.example.memotrip_kroniq.navigation

import java.net.URI
import java.util.UUID

/** No Android dependencies: identical validation for cold and warm intents. */
object ResetPasswordLinkParser {
    private val parameter = Regex("token=([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})")

    fun enabled(flavor: String, buildType: String) =
        flavor == "development" && buildType == "debug"

    fun parse(url: String?): String? {
        if (url == null || url.length > 256) return null
        val uri = try { URI(url) } catch (_: Exception) { return null }
        if (uri.isOpaque || uri.rawUserInfo != null || uri.port != -1) return null
        val value = when {
            uri.scheme == "https" &&
                uri.rawAuthority == "memotrip-bff-development.up.railway.app" &&
                uri.rawPath == "/auth/reset-password/open" && uri.rawQuery == null -> uri.rawFragment
            // Keep the existing query-based custom scheme, without accepting ambiguity.
            uri.scheme == "memotrip" && uri.rawAuthority == "reset-password" &&
                uri.rawPath.isNullOrEmpty() && uri.rawFragment == null -> uri.rawQuery
            else -> null
        } ?: return null
        val token = parameter.matchEntire(value)?.groupValues?.get(1) ?: return null
        return try { UUID.fromString(token).toString() } catch (_: IllegalArgumentException) { null }
    }
}
