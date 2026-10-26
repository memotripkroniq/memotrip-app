package com.example.memotrip_kroniq.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SignupState {
    object Idle : SignupState()
    object Loading : SignupState()
    data object Success : SignupState()
    data class Error(val message: String) : SignupState()
}

class SignupViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SignupState>(SignupState.Idle)
    val state: StateFlow<SignupState> = _state

    fun signup(email: String, password: String, name: String?) {
        _state.value = SignupState.Loading

        viewModelScope.launch {
            try {
                repository.signup(
                    email = email.trim().lowercase(),
                    password = password,
                    name = name,
                    country = "EN"       // 🔥 sem patří
                )

                _state.value = SignupState.Success

            } catch (e: Exception) {
                _state.value = SignupState.Error("Signup failed")
            }
        }
    }

    fun signupWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.value = SignupState.Loading

            try {
                repository.loginWithGoogle(idToken)

                _state.value = SignupState.Success

            } catch (e: Exception) {
                _state.value = SignupState.Error(e.message ?: "Google signup failed")
            }
        }
    }


    fun setError(message: String) {
        _state.value = SignupState.Error(message)
    }
}

