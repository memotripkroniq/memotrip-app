package com.example.memotrip_kroniq.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Public UI state deliberately contains no credentials or backend exception messages. */
data class ResetLinkState(
    val generation: Long = 0,
    val pendingNavigation: Boolean = false,
    val active: Boolean = false,
    val valid: Boolean = false,
    val saving: Boolean = false,
    val completed: Boolean = false,
    val failed: Boolean = false
)

class ResetPasswordLinkViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(ResetLinkState())
    val state = mutableState.asStateFlow()
    private var token: String? = null
    private var submission: Job? = null

    fun accept(url: String?) {
        submission?.cancel()
        token = ResetPasswordLinkParser.parse(url)
        mutableState.value = ResetLinkState(
            generation = state.value.generation + 1,
            pendingNavigation = true, active = true, valid = token != null
        )
    }

    fun navigated(generation: Long) {
        if (state.value.generation == generation) {
            mutableState.value = state.value.copy(pendingNavigation = false)
        }
    }

    fun mayLeaveSplash() = !state.value.active

    fun submit(password: String, reset: suspend (String, String) -> Unit) {
        val current = state.value
        val credential = token ?: return
        if (!current.active || current.saving || current.completed || password.isBlank()) return
        mutableState.value = current.copy(saving = true, failed = false)
        // Password exists only in this request coroutine, never in saved state.
        submission = viewModelScope.launch {
            try {
                reset(credential, password)
                if (state.value.generation == current.generation) {
                    token = null
                    mutableState.value = state.value.copy(saving = false, valid = false, completed = true)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (state.value.generation == current.generation) {
                    mutableState.value = state.value.copy(saving = false, failed = true)
                }
            }
        }
    }

    fun clear() {
        submission?.cancel()
        submission = null
        token = null
        mutableState.value = ResetLinkState(generation = state.value.generation + 1)
    }

    override fun onCleared() { clear() }
}
