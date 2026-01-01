package com.example.memotrip_kroniq.data.tripmap

import com.example.memotrip_kroniq.ui.addtrip.TransportType

interface TripMapGenerator {

    suspend fun generate(
        from: String,
        to: String,
        transport: TransportType
    ): String
}
