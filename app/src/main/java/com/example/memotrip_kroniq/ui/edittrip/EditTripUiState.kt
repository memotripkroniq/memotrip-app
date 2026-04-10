package com.example.memotrip_kroniq.ui.edittrip

import com.example.memotrip_kroniq.ui.addtrip.AddTripUiState

data class EditTripUiState(
    val formState: AddTripUiState = AddTripUiState(),
    val isInitialLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
