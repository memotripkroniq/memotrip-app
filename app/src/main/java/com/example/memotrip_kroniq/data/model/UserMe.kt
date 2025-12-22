package com.example.memotrip_kroniq.data.model

data class UserMe(
    val id: String,
    val email: String,
    val name: String?,
    val isPremium: Boolean,
    val isKroniq: Boolean
)
