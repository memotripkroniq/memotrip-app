package com.example.memotrip_kroniq.data.remote

import com.example.memotrip_kroniq.data.remote.dto.CreateTripRequest
import com.example.memotrip_kroniq.data.remote.dto.CreateTripResponse
import com.example.memotrip_kroniq.data.remote.dto.SimpleSuccessResponse
import com.example.memotrip_kroniq.data.remote.dto.TripDetailDto
import com.example.memotrip_kroniq.data.remote.dto.TripDto
import com.example.memotrip_kroniq.data.remote.dto.TripPhotoCategoryResponse
import com.example.memotrip_kroniq.data.remote.dto.TripPhotoLimitsResponse
import com.example.memotrip_kroniq.data.remote.dto.TripKroniqShareResponse
import com.example.memotrip_kroniq.data.remote.dto.TripPhotosResponse
import com.example.memotrip_kroniq.data.remote.dto.UploadCoverResponse
import com.example.memotrip_kroniq.data.remote.dto.UploadTripPhotoResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import com.example.memotrip_kroniq.data.remote.dto.TripDetailUpdateDto
import retrofit2.http.DELETE
import retrofit2.http.PATCH

interface TripsApi {

    @Multipart
    @POST("trips/cover")
    suspend fun uploadTripCover(
        @Part file: MultipartBody.Part
    ): UploadCoverResponse

    @POST("trips")
    suspend fun createTrip(
        @Body request: CreateTripRequest
    ): CreateTripResponse

    @GET("trips/my")
    suspend fun getMyTrips(): List<TripDto>

    @GET("trips/{tripId}")
    suspend fun getTripDetail(
        @Path("tripId") tripId: String
    ): TripDetailDto

    @GET("trips/{tripId}/photos")
    suspend fun getTripPhotos(
        @Path("tripId") tripId: String
    ): TripPhotosResponse

    @GET("trips/{tripId}/limits/photos")
    suspend fun getTripPhotoLimits(
        @Path("tripId") tripId: String
    ): TripPhotoLimitsResponse

    @Multipart
    @POST("trips/{tripId}/photos")
    suspend fun uploadTripPhoto(
        @Path("tripId") tripId: String,
        @Part file: MultipartBody.Part,
        @Part("categoryId") categoryId: RequestBody? = null
    ): UploadTripPhotoResponse

    @POST("trips/{tripId}/photo-categories")
    suspend fun createTripPhotoCategory(
        @Path("tripId") tripId: String,
        @Body body: Map<String, String>
    ): TripPhotoCategoryResponse

    @PATCH("trips/{tripId}/photo-categories/{categoryId}")
    suspend fun renameTripPhotoCategory(
        @Path("tripId") tripId: String,
        @Path("categoryId") categoryId: String,
        @Body body: Map<String, String>
    ): TripPhotoCategoryResponse

    @DELETE("trips/{tripId}/photo-categories/{categoryId}")
    suspend fun deleteTripPhotoCategory(
        @Path("tripId") tripId: String,
        @Path("categoryId") categoryId: String
    ): SimpleSuccessResponse

    @DELETE("trips/{tripId}/photos/{photoId}")
    suspend fun deleteTripPhoto(
        @Path("tripId") tripId: String,
        @Path("photoId") photoId: String
    ): SimpleSuccessResponse

    @POST("trips/{tripId}/kroniq-share")
    suspend fun shareTripInKroniq(
        @Path("tripId") tripId: String
    ): TripKroniqShareResponse

    @DELETE("trips/{tripId}/kroniq-share")
    suspend fun unshareTripInKroniq(
        @Path("tripId") tripId: String
    ): TripKroniqShareResponse

    @PATCH("trips/{tripId}")
    suspend fun updateTripDetail(
        @Path("tripId") tripId: String,
        @Body body: TripDetailUpdateDto
    ): TripDetailDto

    @DELETE("trips/{tripId}")
    suspend fun deleteTrip(
        @Path("tripId") tripId: String
    )

}
