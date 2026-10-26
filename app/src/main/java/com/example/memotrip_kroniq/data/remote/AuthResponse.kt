package com.example.memotrip_kroniq.data.remote

import com.example.memotrip_kroniq.data.datastore.AuthSession
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val refreshTokenExpiresAt: String,
    val sessionId: String,
    val userId: String
) {
    fun toSession(now: Long = System.currentTimeMillis()): AuthSession {
        val uuid = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
        require(!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) { "Incomplete authentication response" }
        require(tokenType == "Bearer" && expiresIn in 1..(Long.MAX_VALUE - now) / 1000) { "Invalid authentication lifetime" }
        require(userId != null && uuid.matches(userId) && sessionId != null && uuid.matches(sessionId)) { "Invalid session identity" }
        require(refreshTokenExpiresAt != null && Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}Z").matches(refreshTokenExpiresAt)) { "Invalid session expiry" }
        val expiry = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }.parse(refreshTokenExpiresAt) ?: error("Invalid session expiry")
        require(expiry.time > now) { "Session has expired" }
        return AuthSession(accessToken, refreshToken, tokenType, expiresIn, refreshTokenExpiresAt, sessionId, userId, now)
    }
    override fun toString() = "AuthResponse(<redacted>)"
}
