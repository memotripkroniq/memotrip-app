package com.example.memotrip_kroniq.data.remote.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripDetailDto(
    val id: String = "",
    val ownerId: String? = null,

    // ✅ udělej tolerantní
    val name: String? = null,
    val destination: String? = null,
    val transport: String? = null,
    val from: String? = null,
    val to: String? = null,
    val waypoints: List<String> = emptyList(),
    val startDate: String? = null,
    val endDate: String? = null,
    val theme: String? = null,

    val coverImageUrl: String? = null,
    val mapImageUrl: String? = null,
    val mapImageFullUrl: String? = null,

    val plannedBudget: String? = null,
    val spentBudget: String? = null,
    val isSharedInKroniQ: Boolean = false,

    @SerializedName("TripChecklistItems")
    val tripChecklistItems: List<TripChecklistItemDto> = emptyList(),

    @SerializedName("TripNotes")
    val tripNotes: List<TripNoteDto> = emptyList(),

    @SerializedName("TripTipsAndTrips")
    val tripTipsAndTrips: List<TripTipAndTripDto> = emptyList(),
) {
    @Serializable
    data class TripChecklistItemDto(
        val text: String? = null,
        val checked: Boolean? = null,
        val order: Int? = null,
    )

    @Serializable
    data class TripNoteDto(
        val text: String? = null,
        val order: Int? = null,
    )

    @Serializable
    data class TripTipAndTripDto(
        val title: String? = null,
        val imageUrl: String? = null,
        val order: Int? = null,
    )
}
