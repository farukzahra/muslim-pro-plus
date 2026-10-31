package com.farukzahra.muslimplus.adhan

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.farukzahra.muslimplus.R

/**
 * Gravacoes embarcadas do chamado. A primeira e CC0; as demais vem do acervo
 * publico "Azaan of World Mosques" (archive.org) ou do Wikimedia Commons.
 * As atribuicoes ficam na tela de Licencas.
 */
enum class AdhanSound(
    val id: String,
    @RawRes val resId: Int,
    @StringRes val labelRes: Int,
) {
    CLASSIC("classic", R.raw.adhan_classic, R.string.adhan_sound_classic),
    MAKKAH_MULLA("makkah-mulla", R.raw.adhan_makkah_mulla, R.string.adhan_sound_makkah_mulla),
    MAKKAH_HAKIM("makkah-hakim", R.raw.adhan_makkah_hakim, R.string.adhan_sound_makkah_hakim),
    MAKKAH_ABDULMALIK(
        "makkah-abdulmalik",
        R.raw.adhan_makkah_abdulmalik,
        R.string.adhan_sound_makkah_mulla_abdulmalik,
    ),
    MADINAH_BUKHARI("madinah-bukhari", R.raw.adhan_madinah_bukhari, R.string.adhan_sound_madinah_bukhari),
    MADINAH_NAFMAN("madinah-nafman", R.raw.adhan_madinah_nafman, R.string.adhan_sound_madinah_nafman),
    AQSA("aqsa", R.raw.adhan_aqsa, R.string.adhan_sound_aqsa),
    MADINAH_ABDULAZIZ(
        "madinah-abdulaziz",
        R.raw.adhan_madinah_abdulaziz,
        R.string.adhan_sound_madinah_abdulaziz,
    ),
    MAKKAH_KHASHOGGI(
        "makkah-khashoggi",
        R.raw.adhan_makkah_khashoggi,
        R.string.adhan_sound_makkah_khashoggi,
    ),
    ;

    companion object {
        val default: AdhanSound get() = CLASSIC

        fun find(id: String?): AdhanSound = entries.find { it.id == id } ?: default
    }
}
