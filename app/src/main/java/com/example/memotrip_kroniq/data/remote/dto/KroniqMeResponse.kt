package com.example.memotrip_kroniq.data.remote.dto

data class KroniqMeResponse(
    val kroniqImageUrl: String? = null,
    val members: List<KroniqMemberDto> = emptyList()
)

data class KroniqMemberDto(
    val id: String,
    val email: String,
    val name: String? = null,
    val role: String? = null,
    val profileImageUrl: String? = null,
    val expiresAt: String? = null
)
