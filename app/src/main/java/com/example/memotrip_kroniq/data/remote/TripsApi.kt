package com.example.memotrip_kroniq.data.remote

import com.example.memotrip_kroniq.data.remote.dto.CreateTripRequest
import com.example.memotrip_kroniq.data.remote.dto.CreateTripResponse
import com.example.memotrip_kroniq.data.remote.dto.TripDto
import com.example.memotrip_kroniq.data.remote.dto.UploadCoverResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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
}
