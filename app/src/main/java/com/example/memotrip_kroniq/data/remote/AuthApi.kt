package com.example.memotrip_kroniq.data.remote

import LoginRequest
import LoginResponse
import com.example.memotrip_kroniq.data.model.UserMe
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Query
import com.example.memotrip_kroniq.data.remote.dto.TripLimitsResponse
import com.example.memotrip_kroniq.data.remote.dto.DeleteProfileImageResponse
import com.example.memotrip_kroniq.data.remote.dto.ProfileImageResponse
import okhttp3.MultipartBody
import retrofit2.http.PATCH
import retrofit2.http.Part


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

    @PATCH("auth/me")
    suspend fun updateMe(
        @Body body: Map<String, String>
    ): UserMe

    @Multipart
    @POST("auth/me/photo")
    suspend fun uploadProfilePhoto(
        @Part file: MultipartBody.Part
    ): ProfileImageResponse

    @DELETE("auth/me/photo")
    suspend fun deleteProfilePhoto(): DeleteProfileImageResponse

    @POST("/auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>)

    @GET("auth/limits/trips")
    suspend fun getTripLimits(): TripLimitsResponse


}
