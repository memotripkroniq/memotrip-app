package com.example.memotrip_kroniq.data.remote

import LoginRequest
import LoginResponse
import com.example.memotrip_kroniq.data.model.UserMe
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): SignupResponse

    @POST("auth/google")
    suspend fun loginWithGoogle(
        @Body body: Map<String, String>
    ): AuthResponse

    @GET("auth/check-email")
    suspend fun checkEmail(
        @Query("email")
        email: String
    ): CheckEmailResponse

    @GET("auth/me")
    suspend fun getMe(): UserMe

    @POST("/auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>)

}
