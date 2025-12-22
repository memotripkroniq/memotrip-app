package com.example.memotrip_kroniq.ui.auth.google

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.example.memotrip_kroniq.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInCredential

class GoogleAuthManager(private val context: Context) {

    private val oneTapClient = Identity.getSignInClient(context)

    // 🔥 Build request
    private val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .setAutoSelectEnabled(false) // nechceme auto login
        .build()

    /**
     * 🔥 Spustí Google Sign-In — tento kód MUSÍ být bez coroutine!
     */
    fun beginSignIn(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                val request = IntentSenderRequest.Builder(
                    result.pendingIntent.intentSender
                ).build()

                launcher.launch(request)
            }
            .addOnFailureListener { e ->
                Log.e("GoogleAuth", "Google Sign-In failed: ${e.message}")
            }
    }

    /**
     * 🔥 Extrahuje Google ID token po návratu
     */
    fun extractGoogleToken(data: Intent): String? {
        return try {
            val credential: SignInCredential =
                oneTapClient.getSignInCredentialFromIntent(data)

            credential.googleIdToken
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Extract token error: ${e.message}")
            null
        }
    }
}
