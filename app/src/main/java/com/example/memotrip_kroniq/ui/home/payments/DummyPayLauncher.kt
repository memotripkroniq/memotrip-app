package com.example.memotrip_kroniq.ui.home.payments

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun launchDummyPayUrl(
    context: Context,
    paymentUrl: String
) {
    val uri = Uri.parse(paymentUrl)

    runCatching {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, uri)
    }.getOrElse {
        val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        runCatching { context.startActivity(fallbackIntent) }
            .getOrElse { throw ActivityNotFoundException("Unable to open payment page.") }
    }
}
