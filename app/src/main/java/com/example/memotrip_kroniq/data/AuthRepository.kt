package com.example.memotrip_kroniq.data

import android.content.ContentResolver
import android.net.Uri
import LoginRequest
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.image.ImageUploadNormalizer
import com.example.memotrip_kroniq.data.remote.dto.TripLimitsResponse
import com.example.memotrip_kroniq.data.remote.dto.ChangePasswordResponse
import com.example.memotrip_kroniq.data.remote.dto.KroniqMeResponse
import com.example.memotrip_kroniq.data.remote.dto.AddKroniqMemberResponse
import com.example.memotrip_kroniq.data.remote.dto.AddKroniqGuestResponse
import com.example.memotrip_kroniq.data.model.UserMe
import com.example.memotrip_kroniq.data.remote.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import com.example.memotrip_kroniq.ui.home.HomeAuthDataSource

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenDataStore
) : HomeAuthDataSource {

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

    suspend fun checkEmail(email: String): Boolean {
        try {
            return api.checkEmail(email.trim().lowercase()).exists
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
    override suspend fun getMe() =
        api.getMe()

    // ============================================================
    // ⭐ GET TRIP LIMITS
    // ============================================================
    override suspend fun getTripLimits(): TripLimitsResponse =
        api.getTripLimits()

    // ============================================================
    // ⭐ COMMON ERROR PARSER
    // ============================================================
    suspend fun forgotPassword(email: String) {
        api.forgotPassword(
            mapOf("email" to email)
        )
    }

    suspend fun resetPassword(
        token: String,
        newPassword: String
    ) {
        try {
            api.resetPassword(
                mapOf(
                    "token" to token,
                    "newPassword" to newPassword
                )
            )
        } catch (e: HttpException) {
            throw extractApiError(e)
        }
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

    suspend fun deleteAccount(
        currentPassword: String? = null,
        googleIdToken: String? = null
    ): Boolean {
        try {
            val body = linkedMapOf<String, String>().apply {
                currentPassword
                    ?.takeIf { it.isNotBlank() }
                    ?.let { put("currentPassword", it) }
                googleIdToken
                    ?.takeIf { it.isNotBlank() }
                    ?.let { put("googleIdToken", it) }
            }
            return api.deleteAccount(body).success
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
        val normalizedImage = withContext(Dispatchers.Default) {
            ImageUploadNormalizer.normalize(
                contentResolver = contentResolver,
                uri = uri,
                filenamePrefix = "profile"
            )
        }

        val requestBody = normalizedImage.bytes.toRequestBody(normalizedImage.mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = normalizedImage.filename,
            body = requestBody
        )

        return api.uploadProfilePhoto(part).profileImageUrl
            ?: throw IllegalStateException("Profile photo upload returned null profileImageUrl")
    }

    suspend fun deleteProfilePhoto(): Boolean =
        api.deleteProfilePhoto().success

    suspend fun uploadKroniqPhoto(
        contentResolver: ContentResolver,
        uri: Uri
    ): String {
        val normalizedImage = withContext(Dispatchers.Default) {
            ImageUploadNormalizer.normalize(
                contentResolver = contentResolver,
                uri = uri,
                filenamePrefix = "kroniq"
            )
        }

        val requestBody = normalizedImage.bytes.toRequestBody(normalizedImage.mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = normalizedImage.filename,
            body = requestBody
        )

        return api.uploadKroniqPhoto(part).kroniqImageUrl
            ?: throw IllegalStateException("KroniQ photo upload returned null kroniqImageUrl")
    }

    suspend fun deleteKroniqPhoto(): Boolean =
        api.deleteKroniqPhoto().success

    suspend fun getKroniqMe(): KroniqMeResponse =
        api.getKroniqMe()

    suspend fun addKroniqMember(email: String): AddKroniqMemberResponse {
        try {
            return api.addKroniqMember(
                mapOf("email" to email.trim().lowercase())
            )
        } catch (e: HttpException) {
            throw extractApiError(e)
        }
    }

    suspend fun addKroniqGuest(email: String): AddKroniqGuestResponse {
        try {
            return api.addKroniqGuest(
                mapOf("email" to email.trim().lowercase())
            )
        } catch (e: HttpException) {
            throw extractApiError(e)
        }
    }

    suspend fun deleteKroniqMember(memberId: String): Boolean {
        try {
            return api.deleteKroniqMember(memberId).success
        } catch (e: HttpException) {
            throw extractApiError(e)
        }
    }


}
