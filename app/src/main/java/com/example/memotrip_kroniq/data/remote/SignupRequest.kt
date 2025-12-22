package com.example.memotrip_kroniq.data.remote

data class SignupRequest(
    val email: String,
    val password: String,
    val name: String?,
    val country: String
)

