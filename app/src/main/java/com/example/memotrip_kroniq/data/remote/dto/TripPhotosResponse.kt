package com.example.memotrip_kroniq.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TripPhotosResponse(
    val categories: List<TripPhotoCategoryDto> = emptyList(),
    val photos: List<TripPhotoDto> = emptyList(),
)

@Serializable
data class TripPhotoCategoryDto(
    val id: String = "",
    val name: String = "",
)

@Serializable
data class TripPhotoDto(
    val id: String = "",
    val imageUrl: String = "",
    val thumbnailUrl: String? = null,
    val categoryId: String? = null,
    val order: Int? = null,
    val createdAt: String? = null,
)
