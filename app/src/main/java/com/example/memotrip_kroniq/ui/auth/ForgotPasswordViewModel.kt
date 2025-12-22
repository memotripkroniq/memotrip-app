package com.example.memotrip_kroniq.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.data.AuthRepository
import kotlinx.coroutines.launch

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    object Success : ForgotPasswordState()
}

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val state = mutableStateOf<ForgotPasswordState>(ForgotPasswordState.Idle)

    fun submit(email: String) {
        if (email.isBlank()) return

        viewModelScope.launch {
            state.value = ForgotPasswordState.Loading

            runCatching {
                authRepository.forgotPassword(email)
            }.onSuccess {
                state.value = ForgotPasswordState.Success
            }.onFailure {
                // 🔐 bezpečnostně i UX správně:
                // vždy se tváříme jako success
                state.value = ForgotPasswordState.Success
            }
        }
    }
}

