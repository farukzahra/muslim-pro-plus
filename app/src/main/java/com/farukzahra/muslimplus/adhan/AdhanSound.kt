package com.farukzahra.muslimplus.adhan

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.farukzahra.muslimplus.R

/**
 * Gravacoes embarcadas do chamado. A primeira e CC0 e a segunda e dominio
 * publico; as atribuicoes ficam na tela de Licencas.
 */
enum class AdhanSound(
    val id: String,
    @RawRes val resId: Int,
    @StringRes val labelRes: Int,
) {
    CLASSIC("classic", R.raw.adhan_classic, R.string.adhan_sound_classic),
    SABAH_FAKHRY("sabah-fakhry", R.raw.adhan_sabah, R.string.adhan_sound_sabah),
    ;

    companion object {
        val default: AdhanSound get() = CLASSIC

        fun find(id: String?): AdhanSound = entries.find { it.id == id } ?: default
    }
}
