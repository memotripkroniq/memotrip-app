package com.example.memotrip_kroniq.data.remote

import com.example.memotrip_kroniq.data.remote.dto.CreateTripRequest
import com.example.memotrip_kroniq.data.remote.dto.CreateTripResponse
import com.example.memotrip_kroniq.data.remote.dto.TripDetailDto
import com.example.memotrip_kroniq.data.remote.dto.TripDto
import com.example.memotrip_kroniq.data.remote.dto.UploadCoverResponse
import okhttp3.MultipartBody
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
