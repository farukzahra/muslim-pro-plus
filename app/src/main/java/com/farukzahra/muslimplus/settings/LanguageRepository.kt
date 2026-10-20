package com.farukzahra.muslimplus.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Persistencia do idioma via AppCompat, que guarda a escolha e recria a
 * activity sozinho. `null` significa "seguir o aparelho".
 */
object LanguageRepository {
    fun currentTag(): String? =
        AppCompatDelegate.getApplicationLocales()
            .toLanguageTags()
            .takeIf { it.isNotBlank() }

    fun set(tag: String?) {
        val locales = if (tag.isNullOrBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
