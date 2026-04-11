package com.example.memotrip_kroniq

import android.app.Application
import com.example.memotrip_kroniq.core.localization.AppLanguage
import com.example.memotrip_kroniq.core.localization.AppLocaleManager
import com.example.memotrip_kroniq.data.datastore.LanguageDataStore
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import kotlinx.coroutines.runBlocking

class MemoTripApp : Application() {

    override fun onCreate() {
        super.onCreate()

        runBlocking {
            val languageTag = LanguageDataStore(this@MemoTripApp).getLanguageTag()
                ?: AppLanguage.default.languageTag
            AppLocaleManager.applyLanguage(languageTag)
        }

        val tokenStore = TokenDataStore(this)
        RetrofitClient.build(tokenStore)
    }
}
