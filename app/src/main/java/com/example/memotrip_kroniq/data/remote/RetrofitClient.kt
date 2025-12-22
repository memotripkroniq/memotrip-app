package com.example.memotrip_kroniq.data.remote

import android.util.Log
import com.example.memotrip_kroniq.BuildConfig
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val TAG = "RetrofitClient"

    private lateinit var tokenStore: TokenDataStore

    fun init(tokenDataStore: TokenDataStore) {
        tokenStore = tokenDataStore
    }

    // BuildConfig už obsahuje správnou URL podle flavoru
    private val BASE_URL: String = BuildConfig.BASE_URL.also {
        Log.d(TAG, "🔌 USING BASE_URL → $it")
    }

    // 🟢 OKHTTP CLIENT S AUTORIZACÍ
    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()

            // =========================
            // 🔐 AUTH INTERCEPTOR
            // =========================
            .addInterceptor { chain ->
                val originalRequest = chain.request()

                val token = runBlocking {
                    tokenStore.accessToken.first()
                }

                val authorizedRequest =
                    if (!token.isNullOrEmpty()) {
                        Log.d(TAG, "🔐 Authorization: Bearer $token")

                        originalRequest.newBuilder()
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                    } else {
                        originalRequest
                    }

                chain.proceed(authorizedRequest)
            }

            // =========================
            // 🔍 LOG INTERCEPTOR (TVŮJ)
            // =========================
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())

                val rawBody = response.peekBody(Long.MAX_VALUE).string()
                Log.d(TAG, "🔥 RAW RESPONSE BODY: $rawBody")

                response
            }

            .build()
    }

    val api: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // musí končit '/'
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}
