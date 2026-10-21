package com.farukzahra.muslimplus.prayer

/**
 * Codec dos ajustes (offsets) por oracao, persistidos como texto curto no
 * DataStore, por exemplo `FAJR=5,ASR=-2`.
 *
 * Regras:
 * - o resultado sempre cobre [Prayer.entries], com 0 quando o valor nao foi
 *   salvo ou quando a string veio corrompida;
 * - valores fora de [MIN_MINUTES]..[MAX_MINUTES] sao limitados na leitura e na
 *   escrita;
 * - chaves desconhecidas sao ignoradas e nunca quebram o app.
 */
object PrayerOffsets {
    const val MIN_MINUTES = -30
    const val MAX_MINUTES = 30

    /** Oracoes ajustaveis na UI; o nascer do sol nao e uma oracao. */
    val adjustable: List<Prayer> = listOf(
        Prayer.FAJR,
        Prayer.DHUHR,
        Prayer.ASR,
        Prayer.MAGHRIB,
        Prayer.ISHA,
    )

    val defaults: Map<Prayer, Int> = Prayer.entries.associateWith { 0 }

    fun clamp(minutes: Int): Int = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)

    /** Omite os zeros para o texto salvo ficar curto e legivel. */
    fun encode(offsets: Map<Prayer, Int>): String =
        Prayer.entries
            .map { prayer -> prayer to clamp(offsets[prayer] ?: 0) }
            .filter { (_, minutes) -> minutes != 0 }
            .joinToString(",") { (prayer, minutes) -> "${prayer.name}=$minutes" }

    fun decode(raw: String?): Map<Prayer, Int> {
        val result = defaults.toMutableMap()
        raw.orEmpty()
            .split(',')
            .map { it.trim() }
            .filter { it.contains('=') }
            .forEach { entry ->
                val name = entry.substringBefore('=').trim()
                val minutes = entry.substringAfter('=').trim().toIntOrNull() ?: return@forEach
                val prayer = Prayer.entries
                    .firstOrNull { it.name.equals(name, ignoreCase = true) }
                    ?: return@forEach
                result[prayer] = clamp(minutes)
            }
        return result.toMap()
    }
}
