package com.example.memotrip_kroniq.ui.addtrip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memotrip_kroniq.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTripViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTripUiState())
    val uiState: StateFlow<AddTripUiState> = _uiState

    init {
        _uiState.update { it.copy(isLoading = true) }
        loadMe()
    }

    private fun loadMe() {
        viewModelScope.launch {
            try {
                val me = authRepository.getMe()

                _uiState.value = _uiState.value.copy(
                    isThemesLocked = !me.isKroniq,   // 🔐 STEJNÉ PRAVIDLO JAKO HOME
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isThemesLocked = true,           // fail-safe
                    isLoading = false
                )
            }
        }
    }

    fun onTripNameChange(value: String) {
        _uiState.update {
            it.copy(tripName = value)
        }
    }

    fun onDestinationSelected(destination: Destination) {
        _uiState.update {
            it.copy(destination = destination)
        }
    }

    fun onThemeSelected(theme: ThemeType) {
        _uiState.update { state ->

            // ⛔ ještě nevíme, jestli je uživatel premium
            if (state.isLoading) {
                state
            }
            // 🔒 zamčeno
            else if (state.isThemesLocked) {
                state
            }
            // 🔓 odemčeno → toggle logika
            else {
                state.copy(
                    selectedTheme =
                        if (state.selectedTheme == theme) null
                        else theme
                )
            }
        }
    }

    fun onTransportSelected(transport: TransportType) {
        _uiState.update { state ->
            state.copy(transport = transport)
        }
    }

    //jako u theme- možnost nevybrat žádný
//    fun onTransportSelected(transport: TransportType) {
//        _uiState.update { state ->
//            state.copy(
//                transport =
//                    if (state.transport == transport) null
//                    else transport
//            )
//        }
//    }


}
