package com.farukzahra.muslimplus.quran

/**
 * Referencia estavel a um verso, no formato "sura:ayah" ("2:255").
 *
 * O codec fica aqui (e nao no repositorio) para poder ser testado em JVM sem
 * Android. Chaves invalidas sao descartadas em [parse] e [decodeAll], nunca
 * lancam excecao: um DataStore antigo ou corrompido nao pode derrubar o leitor.
 */
data class VerseRef(val surah: Int, val ayah: Int) {
    val isValid: Boolean
        get() = surah in SURAH_RANGE && ayah in 1..MAX_AYAH

    fun toKey(): String = "$surah:$ayah"

    companion object {
        val SURAH_RANGE = 1..114

        /** Sura 2 (Al-Baqarah) e a mais longa: 286 versos. */
        const val MAX_AYAH = 286

        /**
         * Somente digitos ASCII: `toIntOrNull` aceita digitos arabes ("٤:٥"),
         * o que quebraria a chave gravada e a comparacao com [toKey].
         */
        private val KEY_PATTERN = Regex("^[0-9]+:[0-9]+$")

        fun of(surah: Int, ayah: Int): VerseRef? =
            VerseRef(surah, ayah).takeIf { it.isValid }

        /** "2:255" -> VerseRef(2, 255). Qualquer outra forma retorna `null`. */
        fun parse(raw: String?): VerseRef? {
            val text = raw?.trim().orEmpty()
            if (!KEY_PATTERN.matches(text)) return null
            val parts = text.split(':')
            val surah = parts[0].toIntOrNull() ?: return null
            val ayah = parts[1].toIntOrNull() ?: return null
            return of(surah, ayah)
        }

        fun encodeAll(refs: Collection<VerseRef>): Set<String> =
            refs.filter { it.isValid }.map { it.toKey() }.toSet()

        fun decodeAll(raw: Collection<String>?): Set<VerseRef> =
            raw.orEmpty().mapNotNull(::parse).toSet()
    }
}
