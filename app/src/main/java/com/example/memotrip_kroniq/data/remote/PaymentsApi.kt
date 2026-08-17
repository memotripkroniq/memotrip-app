package com.example.memotrip_kroniq.data.remote

import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentRequest
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentsApi {

    @POST("payments/dummy")
    suspend fun createDummyPayment(
        @Body request: DummyPaymentRequest
    ): DummyPaymentResponse
}
