package com.example.memotrip_kroniq.ui.addtrip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.location.LocationSearchRepository
import com.example.memotrip_kroniq.data.tripmap.TripMapGenerator
import com.example.memotrip_kroniq.data.trips.TripsRepository

class AddTripViewModelFactory(
    private val tripsRepository: TripsRepository,
    private val authRepository: AuthRepository,
    private val locationSearchRepository: LocationSearchRepository,
    private val tripMapGenerator: TripMapGenerator
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTripViewModel(
                tripsRepository = tripsRepository,
                authRepository = authRepository,
                locationSearchRepository = locationSearchRepository,
                tripMapGenerator = tripMapGenerator
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
