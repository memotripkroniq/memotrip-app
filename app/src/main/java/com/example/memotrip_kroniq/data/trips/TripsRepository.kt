package com.example.memotrip_kroniq.data.trips

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.memotrip_kroniq.data.remote.TripsApi
import com.example.memotrip_kroniq.data.remote.dto.CreateTripRequest
import com.example.memotrip_kroniq.data.remote.dto.CreateTripResponse
import com.example.memotrip_kroniq.data.remote.dto.TripDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class TripsRepository(
    private val api: TripsApi,
    private val contentResolver: ContentResolver
) {

    suspend fun createTrip(request: CreateTripRequest): CreateTripResponse {
        val created = api.createTrip(request)
        Log.d("CREATE_TRIP", "created=$created")
        return created
    }

    suspend fun getMyTrips(): List<TripDto> =
        api.getMyTrips()

    suspend fun uploadCoverImage(uri: Uri): String {
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Cannot open input stream for uri=$uri")

        // Lepší: zkusíme zjistit content type z ContentResolveru (u PhotoPickeru to bývá image/jpeg)
        val mime = contentResolver.getType(uri) ?: "image/jpeg"
        val ext = when (mime.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }

        val requestBody = bytes.toRequestBody(mime.toMediaType())
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = "cover.$ext",
            body = requestBody
        )

        val response = api.uploadTripCover(part)

        Log.d("COVER_UPLOAD", "upload response=$response")
        Log.d("COVER_UPLOAD", "coverImageUrl=${response.coverImageUrl}")

        return response.coverImageUrl
    }
}