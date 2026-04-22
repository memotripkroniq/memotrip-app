package com.example.memotrip_kroniq

import android.app.Application
import com.example.memotrip_kroniq.core.localization.AppLocaleManager
import com.example.memotrip_kroniq.data.datastore.LanguageDataStore
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import kotlinx.coroutines.runBlocking

class MemoTripApp : Application() {

    override fun onCreate() {
        super.onCreate()

        runBlocking {
            val languageDataStore = LanguageDataStore(this@MemoTripApp)
            val languageTag = languageDataStore.getLanguageTag()
                ?: AppLocaleManager.getCurrentLanguage().languageTag
            AppLocaleManager.applyLanguage(languageTag)
            languageDataStore.saveLanguageTag(languageTag)
        }

        val tokenStore = TokenDataStore(this)
        RetrofitClient.build(tokenStore)
    }
}
