package com.example.memotrip_kroniq.data.payments

import com.example.memotrip_kroniq.data.remote.PaymentsApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException

class DummyPaymentActionsTest {
    @Test fun realRetrofitActions_useExistingIdAndEmptyBodyAndParseOptionalUrl() = runTest {
        val paths = mutableListOf<String>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            paths += request.url.encodedPath
            assertEquals("POST", request.method)
            assertEquals(0L, request.body?.contentLength())
            val continuing = request.url.encodedPath.endsWith("/continue")
            val extra = if (continuing) ",\"paymentUrl\":\"https://api.example/payments/dummy/purchase-1?token=fixture\"" else ""
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body(("""{"purchaseId":"purchase-1","provider":"DUMMY_PAY","status":"${if (continuing) "PENDING" else "FAILED"}","plan":"KRONIQ"$extra}""")
                    .toResponseBody("application/json".toMediaType())).build()
        }.build()
        val repo = repository(client)
        val resumed = repo.continueDummyPayment("purchase-1")
        assertEquals("PENDING", resumed.status)
        assertNotNull(resumed.paymentUrl)
        assertFalse(resumed.toString().contains("token="))
        val cancelled = repo.cancelDummyPayment("purchase-1")
        assertEquals("FAILED", cancelled.status)
        assertNull(cancelled.paymentUrl)
        assertEquals(listOf("/payments/dummy/purchase-1/continue", "/payments/dummy/purchase-1/cancel"), paths)
    }

    @Test fun bothActions_preserveHttpStatusAndStructuredErrors() = runTest {
        for (status in listOf(401, 404, 503)) {
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(status).message("Error")
                    .body("""{"message":"Unavailable","error":"FIXTURE_CODE"}""".toResponseBody("application/json".toMediaType())).build()
            }.build()
            val repo = repository(client)
            for (cancel in listOf(false, true)) {
                val error = runCatching {
                    if (cancel) repo.cancelDummyPayment("purchase-1") else repo.continueDummyPayment("purchase-1")
                }.exceptionOrNull()
                assertTrue(error is PaymentApiException)
                assertEquals(status, (error as PaymentApiException).statusCode)
                assertEquals("FIXTURE_CODE", error.apiCode)
            }
        }
    }

    @Test fun bothActions_doNotAutomaticallyRetryAmbiguousTimeout() = runTest {
        var calls = 0
        val timeout = SocketTimeoutException()
        val repo = repository(OkHttpClient.Builder().addInterceptor {
            calls++
            throw timeout
        }.build())
        assertSame(timeout, runCatching { repo.continueDummyPayment("purchase-1") }.exceptionOrNull())
        assertEquals(1, calls)
        assertSame(timeout, runCatching { repo.cancelDummyPayment("purchase-1") }.exceptionOrNull())
        assertEquals(2, calls)
    }

    private fun repository(client: OkHttpClient) = PaymentsRepository(Retrofit.Builder()
        .baseUrl("https://api.example/").client(client)
        .addConverterFactory(GsonConverterFactory.create()).build().create(PaymentsApi::class.java))
}
