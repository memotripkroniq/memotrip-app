package com.example.memotrip_kroniq.data.remote.dto

data class AddKroniqGuestResponse(
    val success: Boolean,
    val guest: KroniqMemberDto? = null
)
