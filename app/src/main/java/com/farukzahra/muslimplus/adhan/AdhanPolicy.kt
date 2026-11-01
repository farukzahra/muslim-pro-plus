package com.farukzahra.muslimplus.adhan

import com.farukzahra.muslimplus.prayer.Prayer

/** Estado do toque do aparelho, sem depender de android.media. */
enum class RingerState { Silent, Vibrate, Normal }

/**
 * Regras puras do chamado: quais oracoes podem tocar, o padrao (as cinco),
 * a decisao de tocar ou nao e a gravacao de cada oracao. Nascer do sol nunca
 * entra.
 */
object AdhanPolicy {
    val eligible: List<Prayer> = listOf(
        Prayer.FAJR,
        Prayer.DHUHR,
        Prayer.ASR,
        Prayer.MAGHRIB,
        Prayer.ISHA,
    )

    /**
     * Desde 01/11/2026 o Fajr tambem comeca ligado, com uma gravacao propria.
     * Quem ja tem a preferencia salva mantem o conjunto salvo.
     */
    val defaultEnabled: Set<Prayer> = eligible.toSet()

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

    /**
     * O Fajr usa a gravacao propria quando o override esta ligado. As outras
     * oracoes usam a padrao. Uma voz de Fajr no lugar da padrao (ou o
     * contrario) cai no padrao daquele grupo, para nao tocar tathwib fora de
     * hora.
     */
    fun resolve(
        prayer: Prayer,
        global: AdhanSound,
        fajr: AdhanSound,
        override: Boolean,
    ): AdhanSound =
        if (prayer == Prayer.FAJR && override) {
            if (fajr.fajrOnly) fajr else AdhanSound.defaultFajr
        } else {
            if (!global.fajrOnly) global else AdhanSound.default
        }
}
