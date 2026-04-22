package com.example.memotrip_kroniq.core.localization

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object AppLocaleManager {

    fun applyLanguage(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTag)
        )
    }

    fun getCurrentLanguage(): AppLanguage {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val appLanguageTag = appLocales[0]?.toLanguageTag()
        if (!appLanguageTag.isNullOrBlank()) {
            return AppLanguage.fromTag(Locale.forLanguageTag(appLanguageTag).language)
        }

        return AppLanguage.fromTag(Locale.getDefault().language)
    }
}
