package com.example.memotrip_kroniq.ui.edittrip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.tripmap.TripMapGenerator
import com.example.memotrip_kroniq.data.trips.TripsRepository

class EditTripViewModelFactory(
    private val tripsRepository: TripsRepository,
    private val authRepository: AuthRepository,
    private val tripMapGenerator: TripMapGenerator,
    private val tripId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditTripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditTripViewModel(
                tripsRepository = tripsRepository,
                authRepository = authRepository,
                tripMapGenerator = tripMapGenerator,
                tripId = tripId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
