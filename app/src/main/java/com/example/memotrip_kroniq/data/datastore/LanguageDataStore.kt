package com.example.memotrip_kroniq.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.languageDataStore by preferencesDataStore(name = "language_prefs")

class LanguageDataStore(private val context: Context) {

    companion object {
        private val LANGUAGE_TAG_KEY = stringPreferencesKey("language_tag")
    }

    val languageTag: Flow<String?> = context.languageDataStore.data.map { prefs ->
        prefs[LANGUAGE_TAG_KEY]
    }

    suspend fun saveLanguageTag(languageTag: String) {
        context.languageDataStore.edit { prefs ->
            prefs[LANGUAGE_TAG_KEY] = languageTag
        }
    }

    suspend fun getLanguageTag(): String? =
        context.languageDataStore.data.first()[LANGUAGE_TAG_KEY]
}
