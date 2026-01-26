package com.example.memotrip_kroniq.ui.addtrip

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.memotrip_kroniq.data.remote.TripsApi
import com.example.memotrip_kroniq.data.trips.TripsRepository

class AddTripViewModelFactory(
    private val tripsRepository: TripsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTripViewModel(
                tripsRepository = tripsRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
