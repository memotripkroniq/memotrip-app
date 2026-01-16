package com.example.memotrip_kroniq.ui.addtrip

enum class AddTripFlowState {
    IDLE,        // normální formulář
    SAVING,      // ukládáme na BE
    SUCCESS,     // uloženo
    ERROR        // chyba uložení
}
