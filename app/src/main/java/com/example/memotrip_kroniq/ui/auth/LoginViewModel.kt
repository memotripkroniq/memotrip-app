package com.example.memotrip_kroniq.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.model.UserMe
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.jvm.java

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: UserMe) : LoginState()
    data class Error(val message: String, val clearPassword: Boolean = false) : LoginState()
}

class LoginViewModel(
    private val repository: AuthRepository,
    private val tokenStore: TokenDataStore
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    // =====================================
    // LOGIN
    // =====================================
    fun login(email: String, password: String) {
        _state.value = LoginState.Loading

        val cleanEmail = email.trim().lowercase()

        viewModelScope.launch {
            try {
                // 🔥 JEDINÝ CALL
                val me = repository.login(cleanEmail, password)

                Log.d("AUTH", "LOGIN SUCCESS → isKroniq = ${me.isKroniq}")

                _state.value = LoginState.Success(me)

            } catch (e: HttpException) {
                val apiError = parseApiError(e)

                when (apiError?.error) {
                    "EMAIL_NOT_FOUND" ->
                        _state.value = LoginState.Error("You must be registered", true)

                    "WRONG_PASSWORD" ->
                        _state.value = LoginState.Error("Incorrect password", true)

                    "NO_PASSWORD_USE_GOOGLE" ->
                        _state.value = LoginState.Error("This account uses Google login")

                    else ->
                        _state.value = LoginState.Error("Login failed")
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error("Network or server error", true)
            }
        }
    }


    // =====================================
    // GOOGLE LOGIN
    // =====================================
    fun loginWithGoogle(idToken: String) {
        _state.value = LoginState.Loading

        viewModelScope.launch {
            try {
                // 1️⃣ Google login → dostaneme TOKENY
                val response = repository.loginWithGoogle(idToken)

                // 2️⃣ uložíme tokeny do DataStore
                tokenStore.saveTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )

                // 3️⃣ zavoláme /auth/me (už s Authorization headerem)
                val me = repository.getMe()

                Log.d("AUTH", "GOOGLE LOGIN → isKroniq = ${me.isKroniq}")

                // 4️⃣ Success vrací USERA
                _state.value = LoginState.Success(user = me)

            } catch (e: Exception) {
                Log.e("AUTH", "Google login failed", e)
                _state.value = LoginState.Error("Google login failed")
            }
        }
    }



    // =====================================
    // ERROR PARSER
    // =====================================
    private fun parseApiError(e: HttpException): ApiError? {
        return try {
            val json = e.response()?.errorBody()?.string()
            Gson().fromJson(json, ApiError::class.java)
        } catch (ex: Exception) {
            null
        }
    }
}
