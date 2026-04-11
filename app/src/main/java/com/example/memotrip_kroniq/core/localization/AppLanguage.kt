package com.example.memotrip_kroniq.core.localization

import androidx.annotation.StringRes
import com.example.memotrip_kroniq.R

enum class AppLanguage(
    val languageTag: String,
    @StringRes val labelRes: Int
) {
    ENGLISH("en", R.string.language_option_english),
    SLOVAK("sk", R.string.language_option_slovak),
    CZECH("cs", R.string.language_option_czech),
    GERMAN("de", R.string.language_option_german),
    POLISH("pl", R.string.language_option_polish),
    SPANISH("es", R.string.language_option_spanish),
    ITALIAN("it", R.string.language_option_italian);

    companion object {
        val default = ENGLISH

        fun fromTag(languageTag: String?): AppLanguage =
            entries.firstOrNull { it.languageTag == languageTag } ?: default
    }
}
