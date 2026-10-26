package com.example.memotrip_kroniq.ui.home

import com.example.memotrip_kroniq.data.model.UserMe
import com.example.memotrip_kroniq.data.payments.DummyPaymentPlan
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentStatusResponse
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentResponse
import com.example.memotrip_kroniq.data.remote.dto.TripDto
import com.example.memotrip_kroniq.data.remote.dto.TripLimitsResponse

interface HomeAuthDataSource {
    suspend fun getMe(): UserMe
    suspend fun getTripLimits(): TripLimitsResponse
}

interface HomeTripsDataSource {
    suspend fun getMyTrips(): List<TripDto>
}

interface HomePaymentsDataSource {
    suspend fun continueDummyPayment(purchaseId: String): DummyPaymentStatusResponse
    suspend fun cancelDummyPayment(purchaseId: String): DummyPaymentStatusResponse
    suspend fun getDummyPaymentStatus(purchaseId: String): DummyPaymentStatusResponse
    suspend fun createDummyPayment(plan: DummyPaymentPlan): DummyPaymentResponse
}
