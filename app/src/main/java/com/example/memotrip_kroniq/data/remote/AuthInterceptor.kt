package com.example.memotrip_kroniq.data.remote

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
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}
