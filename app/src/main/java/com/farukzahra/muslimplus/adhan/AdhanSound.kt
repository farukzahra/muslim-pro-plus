package com.farukzahra.muslimplus.adhan

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.farukzahra.muslimplus.R

/**
 * Pais de origem da gravacao. A bandeira entra como PNG pequeno em
 * drawable-nodpi; o nome traduzido fica nos tres idiomas.
 */
enum class AdhanCountry(
    @StringRes val labelRes: Int,
    @DrawableRes val flagRes: Int,
) {
    SAUDI_ARABIA(R.string.country_saudi_arabia, R.drawable.flag_sa),
    PALESTINE(R.string.country_palestine, R.drawable.flag_ps),
    EGYPT(R.string.country_egypt, R.drawable.flag_eg),
    UAE(R.string.country_uae, R.drawable.flag_ae),
    TUNISIA(R.string.country_tunisia, R.drawable.flag_tn),
    OMAN(R.string.country_oman, R.drawable.flag_om),
    MALAYSIA(R.string.country_malaysia, R.drawable.flag_my),
    ALGERIA(R.string.country_algeria, R.drawable.flag_dz),
    QATAR(R.string.country_qatar, R.drawable.flag_qa),
}

/**
 * Gravacoes embarcadas do chamado. A primeira e CC0; as demais vem do acervo
 * publico "Azaan of World Mosques" (archive.org), do conjunto
 * "Adhan Notifications" ou do Wikimedia Commons. As atribuicoes ficam na tela
 * de Licencas.
 *
 * [fajrOnly] marca as gravacoes que contem o tathwib ("as-salatu khayrun min
 * an-nawm"). Elas so aparecem na lista do Fajr, porque o trecho nao pertence as
 * outras oracoes.
 */
enum class AdhanSound(
    val id: String,
    @RawRes val resId: Int,
    @StringRes val labelRes: Int,
    val country: AdhanCountry?,
    val fajrOnly: Boolean,
    val durationSeconds: Int,
) {
    CLASSIC(
        "classic",
        R.raw.adhan_classic,
        R.string.adhan_sound_classic,
        null,
        false,
        154,
    ),
    MAKKAH_MULLA(
        "makkah-mulla",
        R.raw.adhan_makkah_mulla,
        R.string.adhan_sound_makkah_mulla,
        AdhanCountry.SAUDI_ARABIA,
        false,
        176,
    ),
    MAKKAH_HAKIM(
        "makkah-hakim",
        R.raw.adhan_makkah_hakim,
        R.string.adhan_sound_makkah_hakim,
        AdhanCountry.SAUDI_ARABIA,
        false,
        185,
    ),
    MAKKAH_ABDULMALIK(
        "makkah-abdulmalik",
        R.raw.adhan_makkah_abdulmalik,
        R.string.adhan_sound_makkah_mulla_abdulmalik,
        AdhanCountry.SAUDI_ARABIA,
        false,
        202,
    ),
    MADINAH_BUKHARI(
        "madinah-bukhari",
        R.raw.adhan_madinah_bukhari,
        R.string.adhan_sound_madinah_bukhari,
        AdhanCountry.SAUDI_ARABIA,
        true,
        325,
    ),
    MADINAH_NAFMAN(
        "madinah-nafman",
        R.raw.adhan_madinah_nafman,
        R.string.adhan_sound_madinah_nafman,
        AdhanCountry.SAUDI_ARABIA,
        true,
        348,
    ),
    AQSA(
        "aqsa",
        R.raw.adhan_aqsa,
        R.string.adhan_sound_aqsa,
        AdhanCountry.PALESTINE,
        false,
        227,
    ),
    MADINAH_ABDULAZIZ(
        "madinah-abdulaziz",
        R.raw.adhan_madinah_abdulaziz,
        R.string.adhan_sound_madinah_abdulaziz,
        AdhanCountry.SAUDI_ARABIA,
        false,
        277,
    ),
    MAKKAH_KHASHOGGI(
        "makkah-khashoggi",
        R.raw.adhan_makkah_khashoggi,
        R.string.adhan_sound_makkah_khashoggi,
        AdhanCountry.SAUDI_ARABIA,
        false,
        190,
    ),
    QATAMI(
        "qatami",
        R.raw.adhan_qatami,
        R.string.adhan_sound_qatami,
        AdhanCountry.SAUDI_ARABIA,
        false,
        132,
    ),
    QATAR_IMADI(
        "qatar-imadi",
        R.raw.adhan_qatar_imadi,
        R.string.adhan_sound_qatar_imadi,
        AdhanCountry.QATAR,
        false,
        179,
    ),
    CAIRO_BANNA(
        "cairo-banna",
        R.raw.adhan_cairo_banna,
        R.string.adhan_sound_cairo_banna,
        AdhanCountry.EGYPT,
        false,
        155,
    ),
    MADINAH_HAMATHANI(
        "madinah-hamathani",
        R.raw.adhan_madinah_hamathani,
        R.string.adhan_sound_madinah_hamathani,
        AdhanCountry.SAUDI_ARABIA,
        false,
        219,
    ),
    CAIRO_SHUAISHA(
        "cairo-shuaisha",
        R.raw.adhan_cairo_shuaisha,
        R.string.adhan_sound_cairo_shuaisha,
        AdhanCountry.EGYPT,
        false,
        234,
    ),
    AJMAN(
        "ajman",
        R.raw.adhan_ajman,
        R.string.adhan_sound_ajman,
        AdhanCountry.UAE,
        false,
        140,
    ),
    TUNISIA(
        "tunisia",
        R.raw.adhan_tunisia,
        R.string.adhan_sound_tunisia,
        AdhanCountry.TUNISIA,
        false,
        142,
    ),
    OMAN(
        "oman",
        R.raw.adhan_oman,
        R.string.adhan_sound_oman,
        AdhanCountry.OMAN,
        false,
        153,
    ),
    MAGHREB_MOKHTAR(
        "maghreb-mokhtar",
        R.raw.adhan_maghreb_mokhtar,
        R.string.adhan_sound_maghreb_mokhtar,
        AdhanCountry.ALGERIA,
        false,
        195,
    ),
    FAJR_MAKKAH(
        "fajr-makkah",
        R.raw.adhan_fajr_makkah,
        R.string.adhan_sound_fajr_makkah,
        AdhanCountry.SAUDI_ARABIA,
        true,
        215,
    ),
    FAJR_CAIRO(
        "fajr-cairo",
        R.raw.adhan_fajr_cairo,
        R.string.adhan_sound_fajr_cairo,
        AdhanCountry.EGYPT,
        true,
        247,
    ),
    FAJR_MALAYSIA(
        "fajr-malaysia",
        R.raw.adhan_fajr_malaysia,
        R.string.adhan_sound_fajr_malaysia,
        AdhanCountry.MALAYSIA,
        true,
        199,
    ),
    FAJR_UMM_AL_QAIWAIN(
        "fajr-umm-al-qaiwain",
        R.raw.adhan_fajr_umm_al_qaiwain,
        R.string.adhan_sound_fajr_umm_al_qaiwain,
        AdhanCountry.UAE,
        true,
        190,
    ),
    ;

    companion object {
        val default: AdhanSound get() = CLASSIC
        val defaultFajr: AdhanSound get() = MADINAH_NAFMAN

        /** Vozes que podem tocar em qualquer oracao (sem tathwib). */
        val normal: List<AdhanSound> get() = entries.filter { !it.fajrOnly }

        /** Vozes exclusivas do Fajr, todas com tathwib. */
        val fajr: List<AdhanSound> get() = entries.filter { it.fajrOnly }

        fun find(id: String?): AdhanSound = entries.find { it.id == id } ?: default

        /** Id desconhecido ou de Fajr cai no padrao do dia. */
        fun findNormal(id: String?): AdhanSound =
            entries.find { it.id == id && !it.fajrOnly } ?: default

        /** Id desconhecido ou de voz comum cai no padrao do Fajr. */
        fun findFajr(id: String?): AdhanSound =
            entries.find { it.id == id && it.fajrOnly } ?: defaultFajr
    }
}
