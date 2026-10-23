package com.example.memotrip_kroniq.ui.home.payments

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.memotrip_kroniq.BuildConfig
import java.net.URI

// Only the configured API origin is trusted. No speculative payment hosts are allowed.
internal fun isAllowedDummyPayUrl(paymentUrl: String, apiBaseUrl: String): Boolean =
    runCatching {
        val payment = URI(paymentUrl)
        val backend = URI(apiBaseUrl)
        payment.scheme.equals("https", ignoreCase = true) &&
            backend.scheme.equals("https", ignoreCase = true) &&
            payment.host != null && payment.host.equals(backend.host, ignoreCase = true) &&
            payment.rawUserInfo == null && payment.rawFragment == null &&
            (if (payment.port == -1) 443 else payment.port) ==
            (if (backend.port == -1) 443 else backend.port)
    }.getOrDefault(false)

fun launchDummyPayUrl(
    context: Context,
    paymentUrl: String
) {
    require(BuildConfig.FLAVOR != "production" && isAllowedDummyPayUrl(paymentUrl, BuildConfig.BASE_URL)) {
        "Payment URL is not allowed."
    }
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
