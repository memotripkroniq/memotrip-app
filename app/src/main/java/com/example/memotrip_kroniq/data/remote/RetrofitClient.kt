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

    private var retrofit: Retrofit? = null

    fun build(tokenStore: TokenDataStore) {
        if (retrofit != null) return

        Log.d("RetrofitClient", "🔌 USING BASE_URL = ${BuildConfig.BASE_URL}")

        val okHttp = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = runBlocking {
                    tokenStore.accessToken.first()
                }

                Log.d("RetrofitClient", "🔐 ACCESS TOKEN = ${token?.take(20)}")

                val request = if (!token.isNullOrEmpty()) {
                    Log.d("RetrofitClient", "➡️ ADDING Authorization header")
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    Log.w("RetrofitClient", "⚠️ NO TOKEN, sending request WITHOUT auth")
                    chain.request()
                }

                chain.proceed(request)
            }
            .addInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)

                if (request.url.encodedPath.contains("trips")) {
                    val body = response.peekBody(Long.MAX_VALUE).string()
                    Log.d("RetrofitClient", "📦 /trips RESPONSE = $body")
                }

                response
            }

            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi
        get() = retrofit!!.create(AuthApi::class.java)

    val tripsApi: TripsApi
        get() = retrofit!!.create(TripsApi::class.java)
}

