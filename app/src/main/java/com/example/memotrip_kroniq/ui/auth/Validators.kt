package com.example.memotrip_kroniq.ui.auth

object Validators {

    // ================================
    //      EMAIL VALIDATION
    // ================================
    fun validateEmail(email: String): String? {
        val clean = email.trim().lowercase()
        if (clean.isEmpty()) return "Email is required"
        val isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(clean).matches()
        return if (!isValid) "Incorrect email" else null
    }


    // ================================
    //      PASSWORD VALIDATION
    // ================================
    fun validatePassword(password: String): String? {
        if (password.isEmpty()) return "Password is required"
        if (password.length < 8) return "Minimum 8 characters"
        if (!password.any { it.isLetter() }) return "Must contain letter"
        if (!password.any { it.isDigit() }) return "Must contain number"
        return null
    }

    // ================================
    //   PASSWORD MATCHING (SIGN UP)
    // ================================
    fun doPasswordsMatch(pass: String, confirm: String): Boolean {
        return pass == confirm
    }
}
