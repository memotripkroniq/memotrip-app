package com.example.memotrip_kroniq.data.remote.dto

data class AddKroniqMemberResponse(
    val success: Boolean,
    val member: KroniqMemberDto? = null
)
