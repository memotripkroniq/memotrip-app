package com.example.memotrip_kroniq.ui.components

import android.app.Activity
import android.content.Context
import com.example.memotrip_kroniq.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity

fun launchGoogleSignIn(
    context: Context,
    onSuccess: (String) -> Unit,
    onError: () -> Unit
) {
    val activity = context as? Activity ?: return

    val signInClient = Identity.getSignInClient(context)

    val request = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(
                    context.getString(R.string.default_web_client_id)
                )

                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .setAutoSelectEnabled(false)
        .build()

    signInClient.beginSignIn(request)
        .addOnSuccessListener { result ->
            try {
                activity.startIntentSenderForResult(
                    result.pendingIntent.intentSender,
                    1001,
                    null,
                    0,
                    0,
                    0
                )
            } catch (e: Exception) {
                e.printStackTrace()
                onError()
            }
        }
        .addOnFailureListener {
            it.printStackTrace()
            onError()
        }
}