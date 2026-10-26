package com.example.memotrip_kroniq.data.payments

import com.example.memotrip_kroniq.data.remote.PaymentsApi
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentStatusResponse
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentResponse
import com.example.memotrip_kroniq.ui.home.HomePaymentsDataSource
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException

class PaymentApiException(
    val statusCode: Int,
    val apiCode: String?,
    message: String,
    cause: Throwable
) : Exception(message, cause)

class PaymentsRepository(
    private val api: PaymentsApi
) : HomePaymentsDataSource {

    override suspend fun createDummyPayment(plan: DummyPaymentPlan): DummyPaymentResponse {
        try {
            return api.createDummyPayment(plan.toRequest())
        } catch (e: HttpException) {
            throw extractApiError(e)
        }
    }

    override suspend fun getDummyPaymentStatus(purchaseId: String): DummyPaymentStatusResponse {
        try {
            return api.getDummyPaymentStatus(purchaseId)
        } catch (e: HttpException) {
            throw extractApiError(e)
        }
    }

    override suspend fun continueDummyPayment(purchaseId: String): DummyPaymentStatusResponse =
        try { api.continueDummyPayment(purchaseId) } catch (e: HttpException) { throw extractApiError(e) }

    override suspend fun cancelDummyPayment(purchaseId: String): DummyPaymentStatusResponse =
        try { api.cancelDummyPayment(purchaseId) } catch (e: HttpException) { throw extractApiError(e) }

    private fun extractApiError(e: HttpException): Exception {
        val error = runCatching { e.response()?.errorBody()?.string() }.getOrNull()

        val json = runCatching { Gson().fromJson(error ?: "{}", JsonObject::class.java) }.getOrNull()
        val apiCode = runCatching { json?.get("error")?.asString }.getOrNull()
        val message = runCatching {
            json?.get("message")?.let {
                if (it.isJsonArray) it.asJsonArray.joinToString("; ") { value -> value.asString }
                else it.asString
            }
        }.getOrNull()
        return PaymentApiException(
            statusCode = e.code(),
            apiCode = apiCode?.takeIf { it.isNotBlank() },
            message = message?.takeIf { it.isNotBlank() } ?: "Unable to start payment.",
            cause = e
        )
    }
}
