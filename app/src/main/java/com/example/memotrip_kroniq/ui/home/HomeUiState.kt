package com.example.memotrip_kroniq.ui.home

data class HomeUiState(
    val isThemesLocked: Boolean = true,   // 🔒 lock/unlock Themes
    val isLoading: Boolean = true,        // ⏳ loading Home
    val userEmail: String? = null         // (volitelné, ale praktické)
)