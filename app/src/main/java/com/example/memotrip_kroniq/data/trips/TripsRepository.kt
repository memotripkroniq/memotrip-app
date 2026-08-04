package com.example.memotrip_kroniq.data.trips

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.memotrip_kroniq.data.image.ImageUploadNormalizer
import com.example.memotrip_kroniq.data.remote.TripsApi
import com.example.memotrip_kroniq.data.remote.dto.CreateTripRequest
import com.example.memotrip_kroniq.data.remote.dto.CreateTripResponse
import com.example.memotrip_kroniq.data.remote.dto.SimpleSuccessResponse
import com.example.memotrip_kroniq.data.remote.dto.TripDetailDto
import com.example.memotrip_kroniq.data.remote.dto.TripKroniqShareResponse
import com.example.memotrip_kroniq.data.remote.dto.TripPhotoLimitsResponse
import com.example.memotrip_kroniq.data.remote.dto.TripPhotosResponse
import com.example.memotrip_kroniq.data.remote.dto.TripDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.memotrip_kroniq.data.remote.dto.TripDetailUpdateDto
import com.memotrip_kroniq.BuildConfig

class TripsRepository(
    private val api: TripsApi,
    private val contentResolver: ContentResolver
) {

    suspend fun createTrip(request: CreateTripRequest): CreateTripResponse {
        val created = api.createTrip(request)
        if (BuildConfig.DEBUG) {
            Log.d("CREATE_TRIP", "Trip created successfully")
        }
        return created
    }

    suspend fun getMyTrips(): List<TripDto> =
        api.getMyTrips()

    suspend fun getTripDetail(tripId: String): TripDetailDto =
        api.getTripDetail(tripId)

    suspend fun getTripPhotos(tripId: String): TripPhotosResponse =
        api.getTripPhotos(tripId)

    suspend fun getTripPhotoLimits(tripId: String): TripPhotoLimitsResponse =
        api.getTripPhotoLimits(tripId)


    suspend fun uploadCoverImage(uri: Uri): String {
        val normalizedImage = ImageUploadNormalizer.normalize(
            contentResolver = contentResolver,
            uri = uri,
            filenamePrefix = "cover"
        )

        val requestBody = normalizedImage.bytes.toRequestBody(normalizedImage.mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = normalizedImage.filename,
            body = requestBody
        )

        val response = api.uploadTripCover(part)

        if (BuildConfig.DEBUG) {
            Log.d("COVER_UPLOAD", "Cover upload completed")
        }

        return response.coverImageUrl
    }

    suspend fun uploadTripPhoto(tripId: String, uri: Uri, categoryId: String?) {
        val normalizedImage = ImageUploadNormalizer.normalize(
            contentResolver = contentResolver,
            uri = uri,
            filenamePrefix = "trip_photo"
        )

        val requestBody = normalizedImage.bytes.toRequestBody(normalizedImage.mimeType.toMediaType())
        val filePart = MultipartBody.Part.createFormData(
            name = "file",
            filename = normalizedImage.filename,
            body = requestBody
        )
        val categoryPart = categoryId
            ?.takeIf { it.isNotBlank() }
            ?.toRequestBody("text/plain".toMediaType())

        api.uploadTripPhoto(tripId, filePart, categoryPart)
    }

    suspend fun createTripPhotoCategory(tripId: String, name: String) {
        api.createTripPhotoCategory(tripId, mapOf("name" to name))
    }

    suspend fun renameTripPhotoCategory(tripId: String, categoryId: String, name: String) {
        api.renameTripPhotoCategory(tripId, categoryId, mapOf("name" to name))
    }

    suspend fun deleteTripPhotoCategory(tripId: String, categoryId: String): SimpleSuccessResponse =
        api.deleteTripPhotoCategory(tripId, categoryId)

    suspend fun deleteTripPhoto(tripId: String, photoId: String): SimpleSuccessResponse =
        api.deleteTripPhoto(tripId, photoId)

    suspend fun shareTripInKroniq(tripId: String): TripKroniqShareResponse =
        api.shareTripInKroniq(tripId)

    suspend fun unshareTripInKroniq(tripId: String): TripKroniqShareResponse =
        api.unshareTripInKroniq(tripId)

    suspend fun updateTripDetail(tripId: String, body: TripDetailUpdateDto): TripDetailDto =
        api.updateTripDetail(tripId, body)

    suspend fun deleteTrip(tripId: String) {
        api.deleteTrip(tripId)
    }
}
