package com.example.memotrip_kroniq.ui.tripdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.memotrip_kroniq.data.trips.TripsRepository

class TripDetailViewModelFactory(
    private val tripsRepository: TripsRepository,
    private val tripId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripDetailViewModel(
                tripsRepository = tripsRepository,
                tripId = tripId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
