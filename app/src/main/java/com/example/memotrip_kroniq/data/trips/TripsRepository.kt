package com.example.memotrip_kroniq.data.trips

import com.example.memotrip_kroniq.data.remote.TripsApi
import com.example.memotrip_kroniq.data.remote.dto.CreateTripRequest
import com.example.memotrip_kroniq.data.remote.dto.TripDto

class TripsRepository(
    private val api: TripsApi
) {

    suspend fun createTrip(request: CreateTripRequest) =
        api.createTrip(request)

    suspend fun getMyTrips(): List<TripDto> =
        api.getMyTrips()
}
