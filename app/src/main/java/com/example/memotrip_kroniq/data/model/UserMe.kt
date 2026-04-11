package com.example.memotrip_kroniq.data.model

data class UserMe(
    val id: String,
    val email: String,
    val name: String?,
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val profileImageUrl: String? = null,
    val kroniqImageUrl: String? = null,
    val hasPassword: Boolean = true,
    val isPremium: Boolean,
    val isKroniq: Boolean
)
