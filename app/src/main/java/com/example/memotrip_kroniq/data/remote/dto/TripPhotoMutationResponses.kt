package com.example.memotrip_kroniq.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadTripPhotoResponse(
    val photo: TripPhotoDto
)

@Serializable
data class TripPhotoCategoryResponse(
    val category: TripPhotoCategoryDto
)

@Serializable
data class SimpleSuccessResponse(
    val success: Boolean = false
)

@Serializable
data class TripKroniqShareResponse(
    val success: Boolean = false,
    val isSharedInKroniQ: Boolean = false
)
