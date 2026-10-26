package com.example.memotrip_kroniq.data.payments

import com.example.memotrip_kroniq.data.remote.PaymentsApi
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentStatusResponse
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentRequest
import com.example.memotrip_kroniq.data.remote.dto.DummyPaymentResponse
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException

class PaymentsRepositoryTest {
    @Test fun httpStatusAndStructuredErrorArePreserved() = runTest {
        val http = HttpException(Response.error<DummyPaymentResponse>(400,
            """{"message":["Invalid plan"],"error":"Bad Request"}"""
                .toResponseBody("application/json".toMediaType())))
        val error = runCatching { repository(http).createDummyPayment(DummyPaymentPlan.PREMIUM) }.exceptionOrNull()
        assertTrue(error is PaymentApiException)
        error as PaymentApiException
        assertEquals(400, error.statusCode)
        assertEquals("Bad Request", error.apiCode)
        assertEquals("Invalid plan", error.message)
        assertSame(http, error.cause)
    }

    @Test fun malformedServerErrorStillPreservesStatus() = runTest {
        val http = HttpException(Response.error<DummyPaymentResponse>(503,
            "unavailable".toResponseBody("text/plain".toMediaType())))
        val error = runCatching { repository(http).createDummyPayment(DummyPaymentPlan.KRONIQ) }.exceptionOrNull()
        assertTrue(error is PaymentApiException)
        assertEquals(503, (error as PaymentApiException).statusCode)
    }

    @Test fun timeoutPropagatesWithoutAnotherRequest() = runTest {
        val timeout = SocketTimeoutException()
        var requests = 0
        val api = object : PaymentsApi {
            override suspend fun continueDummyPayment(purchaseId: String): DummyPaymentStatusResponse = getDummyPaymentStatus(purchaseId)
            override suspend fun cancelDummyPayment(purchaseId: String): DummyPaymentStatusResponse = getDummyPaymentStatus(purchaseId)
            override suspend fun getDummyPaymentStatus(purchaseId: String): DummyPaymentStatusResponse = error("Unexpected GET")
            override suspend fun createDummyPayment(request: DummyPaymentRequest): DummyPaymentResponse {
                requests++
                throw timeout
            }
        }
        assertSame(timeout, runCatching {
            PaymentsRepository(api).createDummyPayment(DummyPaymentPlan.PREMIUM)
        }.exceptionOrNull())
        assertEquals(1, requests)
    }

    @Test fun getStatus_preservesHttpErrorAndDoesNotCreatePurchase() = runTest {
        val http = HttpException(Response.error<DummyPaymentStatusResponse>(404,
            """{"message":"DummyPay purchase not found","error":"Not Found"}"""
                .toResponseBody("application/json".toMediaType())))
        val error = runCatching { repository(http).getDummyPaymentStatus("purchase-1") }.exceptionOrNull()
        assertTrue(error is PaymentApiException)
        assertEquals(404, (error as PaymentApiException).statusCode)
        assertEquals("Not Found", error.apiCode)
        assertSame(http, error.cause)
    }

    @Test fun getStatus_returnsJsonFieldsForAllDocumentedStatesWithoutPost() = runTest {
        var posts = 0
        val requests = mutableListOf<String>()
        var status = "PENDING"
        val api = object : PaymentsApi {
            override suspend fun continueDummyPayment(purchaseId: String): DummyPaymentStatusResponse = getDummyPaymentStatus(purchaseId)
            override suspend fun cancelDummyPayment(purchaseId: String): DummyPaymentStatusResponse = getDummyPaymentStatus(purchaseId)
            override suspend fun getDummyPaymentStatus(purchaseId: String): DummyPaymentStatusResponse {
                requests += purchaseId
                return com.google.gson.Gson().fromJson(
                    """{"purchaseId":"$purchaseId","provider":"DUMMY_PAY","status":"$status","plan":"PREMIUM"}""",
                    DummyPaymentStatusResponse::class.java)
            }
            override suspend fun createDummyPayment(request: DummyPaymentRequest): DummyPaymentResponse {
                posts++
                error("Unexpected POST")
            }
        }
        val repo = PaymentsRepository(api)
        for (value in listOf("PENDING", "COMPLETED", "FAILED")) {
            status = value
            val response = repo.getDummyPaymentStatus("purchase-1")
            assertEquals(value, response.status)
            assertEquals("purchase-1", response.purchaseId)
            assertEquals("DUMMY_PAY", response.provider)
            assertEquals("PREMIUM", response.plan)
        }
        assertEquals(listOf("purchase-1", "purchase-1", "purchase-1"), requests)
        assertEquals(0, posts)
    }

    private fun repository(error: Exception) = PaymentsRepository(object : PaymentsApi {
        override suspend fun continueDummyPayment(purchaseId: String): DummyPaymentStatusResponse = getDummyPaymentStatus(purchaseId)
            override suspend fun cancelDummyPayment(purchaseId: String): DummyPaymentStatusResponse = getDummyPaymentStatus(purchaseId)
            override suspend fun getDummyPaymentStatus(purchaseId: String): DummyPaymentStatusResponse = throw error
        override suspend fun createDummyPayment(request: DummyPaymentRequest): DummyPaymentResponse = throw error
    })
}
