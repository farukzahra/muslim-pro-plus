package com.farukzahra.muslimplus.settings

/**
 * Resolve qual locale o app deve usar.
 *
 * Regra: override explícito válido vence; sem override, segue o locale do
 * sistema quando houver tradução; caso contrário, inglês.
 */
object AppLocale {
    val supported: List<String> = listOf("en", "pt-BR", "ar")

    fun resolve(requested: String?, system: String?): String {
        val override = requested?.trim()?.takeIf { it.isNotEmpty() && it != "system" }
        val candidate = override ?: system ?: return "en"
        val language = candidate.replace('_', '-').substringBefore('-')
        return supported.firstOrNull { it.substringBefore('-').equals(language, ignoreCase = true) }
            ?: "en"
    }
}
