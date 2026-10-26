package com.example.memotrip_kroniq.data.payments

import com.example.memotrip_kroniq.data.remote.PaymentsApi
import com.example.memotrip_kroniq.data.remote.dto.PendingDummyPaymentItem
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

    override suspend fun getAllPendingDummyPayments(): List<PendingDummyPaymentItem> {
        val result = mutableListOf<PendingDummyPaymentItem>()
        val purchases = mutableSetOf<String>()
        val cursors = mutableSetOf<String>()
        var cursor: String? = null
        do {
            val page = try { api.getPendingDummyPayments(limit = 20, cursor = cursor) }
                catch (e: HttpException) { throw extractApiError(e) }
            for (item in page.items) {
                check(item.purchaseId.isNotBlank() && purchases.add(item.purchaseId) &&
                    item.provider == "DUMMY_PAY" && item.status == "PENDING" &&
                    DummyPaymentPlan.entries.any { it.apiValue == item.plan } && item.createdAt.isNotBlank()) {
                    "Unexpected pending payment response."
                }
                result += item
            }
            cursor = page.nextCursor
            check(cursor == null || (cursor.isNotBlank() && cursors.add(cursor))) {
                "Invalid pending payment cursor."
            }
        } while (cursor != null)
        return result
    }

    private fun extractApiError(e: HttpException): Exception {
        val error = runCatching { e.response()?.errorBody()?.string() }.getOrNull()

        val json = runCatching { Gson().fromJson(error ?: "{}", JsonObject::class.java) }.getOrNull()
        val apiCode = runCatching { json?.get("code")?.asString?.takeIf { it.isNotBlank() } ?: json?.get("error")?.asString }.getOrNull()
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
