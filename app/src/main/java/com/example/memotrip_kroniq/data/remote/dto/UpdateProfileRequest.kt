package com.example.memotrip_kroniq.data.remote.dto;

data class UpdateProfileRequest(
    val name: String?,
    val gender: String?,
    val firstName: String?,
    val lastName: String?,
    val dateOfBirth: String?
)
