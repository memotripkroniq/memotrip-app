package com.example.memotrip_kroniq.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateTripResponse(
    @SerializedName("id")
    val id: String,

    // server u create vrací "name"
    @SerializedName("name")
    val name: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("coverImageUrl")
    val coverImageUrl: String?
)
