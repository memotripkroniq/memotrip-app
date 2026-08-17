package com.example.memotrip_kroniq.data.payments

import com.example.memotrip_kroniq.data.remote.PaymentsApi
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentResponse
import com.example.memotrip_kroniq.ui.home.HomePaymentsDataSource
import org.json.JSONObject
import retrofit2.HttpException

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

    private fun extractApiError(e: HttpException): Exception {
        val error = e.response()?.errorBody()?.string()

        val message = try {
            JSONObject(error ?: "{}").optString("message", "Unknown error")
        } catch (_: Exception) {
            "Unknown error"
        }

        return Exception(message)
    }
}
