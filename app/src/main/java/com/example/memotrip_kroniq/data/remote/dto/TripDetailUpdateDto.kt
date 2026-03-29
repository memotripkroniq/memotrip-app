package com.example.memotrip_kroniq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripDetailUpdateDto(

    // ───────── CORE FIELDS ─────────
    val name: String? = null,
    val destination: String? = null,
    val transport: String? = null,
    val from: String? = null,
    val to: String? = null,
    val waypoints: List<String>? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val theme: String? = null,

    val coverImageUrl: String? = null,
    val mapImageUrl: String? = null,
    val mapImageFullUrl: String? = null,

    // ───────── BUDGET ─────────
    val plannedBudget: String? = null,
    val spentBudget: String? = null,

    // ───────── CHECKLIST ─────────
    @SerialName("TripChecklistItems")
    val checklistItems: List<ChecklistItemDto> = emptyList(),

    // ───────── NOTES ─────────
    @SerialName("TripNotes")
    val notes: List<NoteDto> = emptyList(),

    // ───────── TIPS & TRIPS ─────────
    @SerialName("TripTipsAndTrips")
    val tipsAndTrips: List<TipAndTripDto> = emptyList(),
) {

    @Serializable
    data class ChecklistItemDto(
        val text: String,
        val checked: Boolean,
        val order: Int,
    )

    @Serializable
    data class NoteDto(
        val text: String,
        val order: Int,
    )

    @Serializable
    data class TipAndTripDto(
        val title: String,
        val imageUrl: String? = null,
        val order: Int,
    )
}