package com.example.memotrip_kroniq.data.remote

import com.example.memotrip_kroniq.data.remote.dto.CreateTripRequest
import com.example.memotrip_kroniq.data.remote.dto.CreateTripResponse
import com.example.memotrip_kroniq.data.remote.dto.TripDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TripsApi {

    @POST("trips")
    suspend fun createTrip(
        @Body request: CreateTripRequest
    ): CreateTripResponse

    @GET("trips/my")
    suspend fun getMyTrips(): List<TripDto>
}
