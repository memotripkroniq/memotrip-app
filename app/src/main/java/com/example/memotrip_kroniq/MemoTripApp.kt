package com.example.memotrip_kroniq

import android.app.Application
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient

class MemoTripApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val tokenStore = TokenDataStore(this)
        RetrofitClient.init(tokenStore)
    }
}
