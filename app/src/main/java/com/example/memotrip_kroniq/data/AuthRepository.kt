package com.example.memotrip_kroniq.data

import android.content.ContentResolver
import android.net.Uri
import LoginRequest
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.dto.TripLimitsResponse
import com.example.memotrip_kroniq.data.remote.dto.ChangePasswordResponse
import com.example.memotrip_kroniq.data.model.UserMe
import com.example.memotrip_kroniq.data.remote.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenDataStore
) {

    // ⭐ SIGNUP
    suspend fun signup(
        email: String,
        password: String,
        name: String?,
        country: String
    ): SignupResponse {
        try {
            return api.signup(
                SignupRequest(
                    email = email.trim().lowercase(),
                    password = password,
                    name = name,
                    country = country
                )
            )
        } catch (e: HttpException) {
            throw extractApiError(e)
        }
    }

    // ⭐ LOGIN
    suspend fun login(email: String, password: String): UserMe {
        try {
            // 1️⃣ LOGIN
            val response = api.login(
                LoginRequest(
                    email = email.trim().lowercase(),
                    password = password
                )
            )

            // 2️⃣ ULOŽ TOKEN – MUSÍ DOBĚHNOUT
            tokenStore.saveAccessToken(response.accessToken)

            // 🔥 3️⃣ TEPRVE TEĎ /me
            return api.getMe()

        } catch (e: HttpException) {
            println("REPO HttpException CODE=${e.code()} message=${e.message()}")
            throw e
        } catch (e: Exception) {
            println("🔥 REPO Unknown login error = ${e.message}")
            throw e
        }
    }



    // ⭐ GOOGLE LOGIN
    suspend fun loginWithGoogle(idToken: String): AuthResponse {
        try {
            return api.loginWithGoogle(mapOf("idToken" to idToken))
        } catch (e: HttpException) {
            throw extractApiError(e)
        }
    }

    // ============================================================
    // ⭐ COMMON ERROR PARSER
    // ============================================================
    private fun extractApiError(e: HttpException): Exception {
        val error = e.response()?.errorBody()?.string()

        val message = try {
            JSONObject(error ?: "{}").optString("message", "Unknown error")
        } catch (json: Exception) {
            "Unknown error"
        }

        return Exception(message)
    }

    // ============================================================
    // ⭐ GET ME
    // ============================================================
    suspend fun getMe() =
        api.getMe()

    // ============================================================
    // ⭐ GET TRIP LIMITS
    // ============================================================
    suspend fun getTripLimits(): TripLimitsResponse =
        api.getTripLimits()

    // ============================================================
    // ⭐ COMMON ERROR PARSER
    // ============================================================
    suspend fun forgotPassword(email: String) {
        api.forgotPassword(
            mapOf("email" to email)
        )
    }

    suspend fun changePassword(
        currentPassword: String?,
        newPassword: String
    ): ChangePasswordResponse {
        try {
            val body = linkedMapOf<String, String>().apply {
                currentPassword
                    ?.takeIf { it.isNotBlank() }
                    ?.let { put("currentPassword", it) }
                put("newPassword", newPassword)
            }
            return api.changePassword(body)
        } catch (e: HttpException) {
            throw extractApiError(e)
        }
    }

    // ============================================================
    // ⭐ UPDATE PROFILE
    // ============================================================
    suspend fun updateMe(request: Map<String, String>): UserMe {
        return api.updateMe(request)
    }

    suspend fun uploadProfilePhoto(
        contentResolver: ContentResolver,
        uri: Uri
    ): String {
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Cannot open input stream for uri=$uri")

        val mime = contentResolver.getType(uri) ?: "image/jpeg"
        val ext = when (mime.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }

        val requestBody = bytes.toRequestBody(mime.toMediaType())
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = "profile.$ext",
            body = requestBody
        )

        return api.uploadProfilePhoto(part).profileImageUrl
            ?: throw IllegalStateException("Profile photo upload returned null profileImageUrl")
    }

    suspend fun deleteProfilePhoto(): Boolean =
        api.deleteProfilePhoto().success


}
