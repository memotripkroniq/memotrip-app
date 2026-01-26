package com.example.memotrip_kroniq.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UploadCoverResponse(
    @SerializedName("coverImageUrl")
    val coverImageUrl: String
)
