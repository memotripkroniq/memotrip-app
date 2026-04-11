package com.example.memotrip_kroniq.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.memotrip_kroniq.R

@Composable
fun authMessage(message: String?): String? {
    return when (message) {
        null -> null
        "You must be registered" -> stringResource(R.string.auth_error_must_be_registered)
        "Incorrect password" -> stringResource(R.string.auth_error_incorrect_password)
        "This account uses Google login" -> stringResource(R.string.auth_error_google_login_only)
        "Login failed" -> stringResource(R.string.auth_error_login_failed)
        "Network or server error" -> stringResource(R.string.auth_error_network_or_server)
        "Google login failed" -> stringResource(R.string.auth_error_google_login_failed)
        "Email is required" -> stringResource(R.string.auth_error_email_required)
        "Incorrect email" -> stringResource(R.string.auth_error_incorrect_email)
        "Password is required" -> stringResource(R.string.auth_error_password_required)
        "Minimum 8 characters" -> stringResource(R.string.auth_error_password_min_length)
        "Must contain letter" -> stringResource(R.string.auth_error_password_letter_required)
        "Must contain number" -> stringResource(R.string.auth_error_password_number_required)
        "Passwords do not match" -> stringResource(R.string.auth_error_passwords_do_not_match)
        "Email already exists" -> stringResource(R.string.auth_error_email_exists)
        "Google signup failed" -> stringResource(R.string.auth_error_google_signup_failed)
        "Signup failed" -> stringResource(R.string.auth_error_signup_failed)
        "Confirm password is required" -> stringResource(R.string.auth_error_confirm_password_required)
        "You must agree our Legal & Policies" -> stringResource(R.string.auth_error_legal_required)
        else -> message
    }
}
