package com.example.memotrip_kroniq.data.remote

import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentRequest
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentResponse
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentStatusResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentsApi {

    @GET("payments/dummy/{purchaseId}/status")
    suspend fun getDummyPaymentStatus(
        @Path("purchaseId") purchaseId: String
    ): DummyPaymentStatusResponse


    @POST("payments/dummy")
    suspend fun createDummyPayment(
        @Body request: DummyPaymentRequest
    ): DummyPaymentResponse
}
