package com.farukzahra.muslimplus.adhan

import com.farukzahra.muslimplus.prayer.Prayer

/** Estado do toque do aparelho, sem depender de android.media. */
enum class RingerState { Silent, Vibrate, Normal }

/**
 * Regras puras do chamado: quais oracoes podem tocar, o padrao (todas menos
 * Fajr) e a decisao de tocar ou nao. Nascer do sol nunca entra.
 */
object AdhanPolicy {
    val eligible: List<Prayer> = listOf(
        Prayer.FAJR,
        Prayer.DHUHR,
        Prayer.ASR,
        Prayer.MAGHRIB,
        Prayer.ISHA,
    )

    val defaultEnabled: Set<Prayer> = eligible.toSet() - Prayer.FAJR

    /** `null` = nunca configurado; conjunto vazio = usuario desligou tudo. */
    fun sanitize(stored: Set<String>?): Set<Prayer> =
        if (stored == null) defaultEnabled else eligible.filter { it.name in stored }.toSet()

    fun encode(prayers: Set<Prayer>): Set<String> =
        prayers.filter { it in eligible }.map { it.name }.toSet()

    fun shouldPlay(
        prayer: Prayer,
        enabled: Set<Prayer>,
        respectSilent: Boolean,
        ringer: RingerState,
    ): Boolean {
        if (prayer !in eligible || prayer !in enabled) return false
        return !respectSilent || ringer == RingerState.Normal
    }
}
