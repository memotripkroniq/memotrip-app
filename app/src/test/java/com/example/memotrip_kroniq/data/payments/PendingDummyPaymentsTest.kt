package com.example.memotrip_kroniq.data.payments

import com.example.memotrip_kroniq.data.remote.PaymentsApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PendingDummyPaymentsTest {
    private fun item(id: String, plan: String = "PREMIUM", status: String = "PENDING") =
        """{"purchaseId":"$id","provider":"DUMMY_PAY","status":"$status","plan":"$plan","createdAt":"2026-10-26T10:00:00.000Z"}"""

    @Test fun recoveryReadsEveryPageWithLimit20AndNoCreateOrUrl() = runTest {
        val requests = mutableListOf<String?>()
        val repo = repository { request ->
            assertEquals("GET", request.method)
            assertEquals("/payments/dummy/pending", request.url.encodedPath)
            assertEquals("20", request.url.queryParameter("limit"))
            val cursor = request.url.queryParameter("cursor")
            requests += cursor
            200 to if (cursor == null) """{"items":[${item("one")}],"nextCursor":"cursor-1"}"""
                else """{"items":[${item("two", "KRONIQ")}],"nextCursor":null}"""
        }
        val result = repo.getAllPendingDummyPayments()
        assertEquals(listOf(null, "cursor-1"), requests)
        assertEquals(listOf("one", "two"), result.map { it.purchaseId })
        assertEquals(listOf("PREMIUM", "KRONIQ"), result.map { it.plan })
    }

    @Test fun failureOnLaterPageNeverReturnsPartialRecovery() = runTest {
        var calls = 0
        val repo = repository {
            calls++
            if (calls == 1) 200 to """{"items":[${item("one")}],"nextCursor":"cursor-1"}"""
            else 503 to """{"code":"UNAVAILABLE","message":"Unavailable"}"""
        }
        val error = runCatching { repo.getAllPendingDummyPayments() }.exceptionOrNull()
        assertTrue(error is PaymentApiException)
        assertEquals(503, (error as PaymentApiException).statusCode)
        assertEquals(2, calls)
    }

    @Test fun repeatedCursorFailsClosedInsteadOfLoopingOrIgnoringLaterPurchases() = runTest {
        var calls = 0
        val repo = repository {
            calls++
            200 to """{"items":[],"nextCursor":"same"}"""
        }
        assertTrue(runCatching { repo.getAllPendingDummyPayments() }.exceptionOrNull() is IllegalStateException)
        assertEquals(2, calls)
    }

    @Test fun malformedItemFailsClosedInsteadOfSilentlyDroppingIt() = runTest {
        for (entry in listOf(item("one", status = "COMPLETED"), item("one", plan = "UNKNOWN"))) {
            val repo = repository { 200 to """{"items":[$entry],"nextCursor":null}""" }
            assertTrue(runCatching { repo.getAllPendingDummyPayments() }.exceptionOrNull() is IllegalStateException)
        }
    }

    @Test fun createConflictRetainsDocumentedCodeAndIsNeverRetried() = runTest {
        var calls = 0
        val repo = repository { request ->
            calls++
            assertEquals("POST", request.method)
            409 to """{"statusCode":409,"code":"DUMMY_PAY_PENDING_EXISTS","message":"Pending exists"}"""
        }
        val error = runCatching { repo.createDummyPayment(DummyPaymentPlan.KRONIQ) }.exceptionOrNull()
        assertTrue(error is PaymentApiException)
        assertEquals("DUMMY_PAY_PENDING_EXISTS", (error as PaymentApiException).apiCode)
        assertEquals(409, error.statusCode)
        assertEquals(1, calls)
    }

    private fun repository(answer: (Request) -> Pair<Int, String>): PaymentsRepository {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val (code, body) = answer(chain.request())
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(code).message("Fixture")
                .body(body.toResponseBody("application/json".toMediaType())).build()
        }.build()
        return PaymentsRepository(Retrofit.Builder().baseUrl("https://api.example/").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(PaymentsApi::class.java))
    }
}
