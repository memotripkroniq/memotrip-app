package com.example.memotrip_kroniq.data.remote

import android.util.Log
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStore: TokenDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val original = chain.request()

        val token = runBlocking {
            tokenStore.accessToken.first()
        }

        val request = if (!token.isNullOrEmpty()) {
            Log.d("AUTH", "➡ Authorization: Bearer $token")

            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}
