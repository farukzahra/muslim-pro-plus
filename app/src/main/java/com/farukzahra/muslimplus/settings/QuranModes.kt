package com.farukzahra.muslimplus.settings

import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/**
 * Camadas do Alcorao ligadas por padrao: sempre o arabe (texto original) mais
 * a traducao do idioma do app. Arabe nao ganha traducao extra porque o texto
 * ja e' o original.
 */
object QuranModes {
    fun defaults(languageTag: String?): Set<String> {
        val language = (languageTag ?: "en")
            .replace('_', '-')
            .substringBefore('-')
            .lowercase(Locale.ROOT)
        return when (language) {
            "pt" -> setOf("ar", "pt-BR")
            "ar" -> setOf("ar")
            else -> setOf("ar", "en")
        }
    }

    /** Idioma efetivo do app: override do AppCompat ou o locale do sistema. */
    fun currentLanguageTag(): String {
        val override = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return if (override.isNotBlank()) override else Locale.getDefault().toLanguageTag()
    }
}
