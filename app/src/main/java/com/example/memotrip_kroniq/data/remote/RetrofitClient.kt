package com.example.memotrip_kroniq.data.remote

import android.util.Log
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.memotrip_kroniq.BuildConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder

object RetrofitClient {

    private var retrofit: Retrofit? = null
    private const val TAG = "RetrofitClient"
    private val isVerboseHttpLoggingEnabled: Boolean
        get() = BuildConfig.DEBUG

    fun build(tokenStore: TokenDataStore) {
        if (retrofit != null) return

        if (isVerboseHttpLoggingEnabled) {
            Log.d(TAG, "Using BASE_URL=${BuildConfig.BASE_URL}")
        }

        val okHttp = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = runBlocking {
                    tokenStore.accessToken.first()
                }

                val request = if (!token.isNullOrEmpty()) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }

                chain.proceed(request)
            }
            .addInterceptor { chain ->
                val request = chain.request()
                val startedAt = System.nanoTime()

                try {
                    val response = chain.proceed(request)
                    val durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)

                    if (isVerboseHttpLoggingEnabled) {
                        Log.d(
                            TAG,
                            "${request.method} ${request.url.encodedPath} code=${response.code} durationMs=$durationMs"
                        )
                    } else if (!response.isSuccessful) {
                        Log.w(
                            TAG,
                            "HTTP error ${request.method} ${request.url.encodedPath} code=${response.code}"
                        )
                    }

                    response
                } catch (e: Exception) {
                    val durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
                    Log.e(
                        TAG,
                        "HTTP request failed ${request.method} ${request.url.encodedPath} after ${durationMs}ms: ${e.javaClass.simpleName}"
                    )
                    throw e
                }
            }

            .build()

        val gson = GsonBuilder()
            .serializeNulls()
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApi: AuthApi
        get() = retrofit!!.create(AuthApi::class.java)

    val tripsApi: TripsApi
        get() = retrofit!!.create(TripsApi::class.java)
}

