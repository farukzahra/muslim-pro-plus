package com.farukzahra.muslimplus.ui.screens.settings

import androidx.annotation.StringRes
import com.farukzahra.muslimplus.R

/**
 * Opcoes de idioma compartilhadas pela tela de idioma e pelo onboarding.
 * `tag == null` significa seguir o aparelho.
 */
data class LanguageOption(val tag: String?, @StringRes val labelRes: Int)

val languageOptions: List<LanguageOption> = listOf(
    LanguageOption(null, R.string.language_system),
    LanguageOption("en", R.string.language_english),
    LanguageOption("pt-BR", R.string.language_portuguese),
    LanguageOption("ar", R.string.language_arabic),
)

/** Rotulo do idioma efetivo: sem override, o app segue o sistema. */
@StringRes
fun languageLabelRes(tag: String?): Int = when {
    tag == null -> R.string.language_system
    tag.startsWith("pt", ignoreCase = true) -> R.string.language_portuguese
    tag.startsWith("ar", ignoreCase = true) -> R.string.language_arabic
    else -> R.string.language_english
}
