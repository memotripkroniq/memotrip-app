package com.example.memotrip_kroniq.data.remote

import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentRequest
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentResponse
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentStatusResponse
import com.example.memotrip_kroniq.data.remote.dto.PendingDummyPaymentsResponse
import retrofit2.http.Query
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentsApi {
    @GET("payments/dummy/pending")
    suspend fun getPendingDummyPayments(
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null
    ): PendingDummyPaymentsResponse


    @GET("payments/dummy/{purchaseId}/status")
    suspend fun getDummyPaymentStatus(
        @Path("purchaseId") purchaseId: String
    ): DummyPaymentStatusResponse


    @POST("payments/dummy/{purchaseId}/continue")
    suspend fun continueDummyPayment(@Path("purchaseId") purchaseId: String): DummyPaymentStatusResponse

    @POST("payments/dummy/{purchaseId}/cancel")
    suspend fun cancelDummyPayment(@Path("purchaseId") purchaseId: String): DummyPaymentStatusResponse

    @POST("payments/dummy")
    suspend fun createDummyPayment(
        @Body request: DummyPaymentRequest
    ): DummyPaymentResponse
}
