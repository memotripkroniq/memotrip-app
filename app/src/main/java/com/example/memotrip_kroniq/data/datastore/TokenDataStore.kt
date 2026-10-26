package com.example.memotrip_kroniq.data.datastore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private val Context.authPreferences by preferencesDataStore(name = "auth_prefs")

class TokenDataStore private constructor(private val store: AuthSessionStore) : AuthSessionStore by store {
    constructor(context: Context) : this(
        EncryptedAuthSessionStore(context.applicationContext.authPreferences, AesGcmSessionCipher { SessionKey.get() })
    )
    suspend fun getAccessToken(): String? = accessToken.first()
}

private object SessionKey {
    private const val ALIAS = "memotrip.auth.session.v1"
    @Synchronized fun get(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build())
        }.generateKey()
    }
}
