package com.farukzahra.muslimplus.quran.audio

import androidx.annotation.RawRes
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.adhan.AdhanCountry
import com.farukzahra.muslimplus.quran.VerseRef

/**
 * Gravação árabe (Hafs) por verso, do acervo público do EveryAyah.
 *
 * O catálogo é grande de propósito: cada voz é um arquivo remoto baixado sob
 * demanda, então não pesa no APK. A bandeira é a do país do recitador, como no
 * catálogo de adhan.
 */
data class QuranReciter(
    override val id: String,
    override val reciter: String,
    val folder: String,
    val bitrateKbps: Int,
    val country: AdhanCountry? = null,
    @RawRes val previewRes: Int,
) : QuranAudioSource {
    init {
        require(id.matches(Regex("[a-z0-9]+(?:-[a-z0-9]+)*")))
        require(folder.matches(Regex("[A-Za-z0-9_-]+")))
        require(bitrateKbps > 0)
    }

    override fun urlFor(surah: Int, ayah: Int): String {
        require(VerseRef.of(surah, ayah) != null && ayah <= QuranReciters.verseCounts[surah - 1])
        return "https://everyayah.com/data/$folder/${fileNameFor(surah, ayah)}"
    }
}

object QuranReciters {
    internal val verseCounts = listOf(7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111, 43, 52, 99, 128, 111, 110, 98, 135, 112, 78, 118, 64, 77, 227, 93, 88, 69, 60, 34, 30, 73, 54, 45, 83, 182, 88, 75, 85, 54, 53, 89, 59, 37, 35, 38, 29, 18, 45, 60, 49, 62, 55, 78, 96, 29, 22, 24, 13, 14, 11, 11, 18, 12, 12, 30, 52, 52, 44, 28, 28, 20, 56, 40, 31, 50, 40, 46, 42, 29, 19, 36, 25, 22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19, 5, 8, 8, 11, 11, 8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4, 5, 6)

    /** Pastas verificadas com HEAD em `001001.mp3` (05/11/2026). */
    val all: List<QuranReciter> = listOf(
        QuranReciter("alafasy-128", "Mishary Alafasy", "Alafasy_128kbps", 128, AdhanCountry.KUWAIT, R.raw.quran_preview_alafasy_128),
        QuranReciter("sudais-192", "Abdurrahmaan As-Sudais", "Abdurrahmaan_As-Sudais_192kbps", 192, AdhanCountry.SAUDI_ARABIA, R.raw.quran_preview_sudais_192),
        QuranReciter("husary-128", "Mahmoud Khalil Al-Husary", "Husary_128kbps", 128, AdhanCountry.EGYPT, R.raw.quran_preview_husary_128),
        QuranReciter("shaatree-128", "Abu Bakr Ash-Shaatree", "Abu_Bakr_Ash-Shaatree_128kbps", 128, AdhanCountry.YEMEN, R.raw.quran_preview_shaatree_128),
        QuranReciter("abdulbasit-murattal-192", "Abdul Basit (Murattal)", "Abdul_Basit_Murattal_192kbps", 192, AdhanCountry.EGYPT, R.raw.quran_preview_abdulbasit_murattal_192),
        QuranReciter("abdulbasit-mujawwad-128", "Abdul Basit (Mujawwad)", "Abdul_Basit_Mujawwad_128kbps", 128, AdhanCountry.EGYPT, R.raw.quran_preview_abdulbasit_mujawwad_128),
        QuranReciter("ayyoub-128", "Muhammad Ayyoub", "Muhammad_Ayyoub_128kbps", 128, AdhanCountry.SAUDI_ARABIA, R.raw.quran_preview_ayyoub_128),
        QuranReciter("minshawy-murattal-128", "Al-Minshawi (Murattal)", "Minshawy_Murattal_128kbps", 128, AdhanCountry.EGYPT, R.raw.quran_preview_minshawy_murattal_128),
        QuranReciter("minshawy-mujawwad-192", "Al-Minshawi (Mujawwad)", "Minshawy_Mujawwad_192kbps", 192, AdhanCountry.EGYPT, R.raw.quran_preview_minshawy_mujawwad_192),
        QuranReciter("hudhaify-128", "Ali Al-Hudhaify", "Hudhaify_128kbps", 128, AdhanCountry.SAUDI_ARABIA, R.raw.quran_preview_hudhaify_128),
        QuranReciter("ghamadi-40", "Saad Al-Ghamdi", "Ghamadi_40kbps", 40, AdhanCountry.SAUDI_ARABIA, R.raw.quran_preview_ghamadi_40),
        QuranReciter("dussary-128", "Yasser Ad-Dussary", "Yasser_Ad-Dussary_128kbps", 128, AdhanCountry.SAUDI_ARABIA, R.raw.quran_preview_dussary_128),
        QuranReciter("qatami-128", "Nasser Al-Qatami", "Nasser_Alqatami_128kbps", 128, AdhanCountry.SAUDI_ARABIA, R.raw.quran_preview_qatami_128),
        QuranReciter("shuraym-128", "Saud Ash-Shuraym", "Saood_ash-Shuraym_128kbps", 128, AdhanCountry.SAUDI_ARABIA, R.raw.quran_preview_shuraym_128),
        QuranReciter("jibreel-128", "Muhammad Jibreel", "Muhammad_Jibreel_128kbps", 128, AdhanCountry.EGYPT, R.raw.quran_preview_jibreel_128),
        QuranReciter("budair-128", "Salah Al-Budair", "Salah_Al_Budair_128kbps", 128, AdhanCountry.SAUDI_ARABIA, R.raw.quran_preview_budair_128),
        QuranReciter("basfar-192", "Abdullah Basfar", "Abdullah_Basfar_192kbps", 192, AdhanCountry.SAUDI_ARABIA, R.raw.quran_preview_basfar_192),
        QuranReciter("rifai-192", "Hani Ar-Rifai", "Hani_Rifai_192kbps", 192, AdhanCountry.SAUDI_ARABIA, R.raw.quran_preview_rifai_192),
        QuranReciter("jaber-64", "Ali Jaber", "Ali_Jaber_64kbps", 64, AdhanCountry.SAUDI_ARABIA, R.raw.quran_preview_jaber_64),
        QuranReciter("abbad-64", "Fares Abbad", "Fares_Abbad_64kbps", 64, AdhanCountry.YEMEN, R.raw.quran_preview_abbad_64),
        QuranReciter("alili-128", "Aziz Alili", "Aziz_Alili_128kbps", 128, AdhanCountry.NORTH_MACEDONIA, R.raw.quran_preview_alili_128),
        QuranReciter("parhizgar-48", "Parhizgar", "Parhizgar_48kbps", 48, AdhanCountry.IRAN, R.raw.quran_preview_parhizgar_48),
        QuranReciter("tablawy-128", "Mohammad Al-Tablaway", "Mohammad_al_Tablaway_128kbps", 128, AdhanCountry.EGYPT, R.raw.quran_preview_tablawy_128),
        QuranReciter("ismail-48", "Mustafa Ismail", "Mustafa_Ismail_48kbps", 48, AdhanCountry.EGYPT, R.raw.quran_preview_ismail_48),
        QuranReciter("abdulkareem-128", "Muhammad AbdulKareem", "Muhammad_AbdulKareem_128kbps", 128, AdhanCountry.SUDAN, R.raw.quran_preview_abdulkareem_128),
        QuranReciter("sowaid-64", "Ayman Sowaid", "Ayman_Sowaid_64kbps", 64, AdhanCountry.IRAQ, R.raw.quran_preview_sowaid_64),
        QuranReciter("neana-128", "Ahmed Neana", "Ahmed_Neana_128kbps", 128, AdhanCountry.KUWAIT, R.raw.quran_preview_neana_128),
    )

    val default: QuranReciter get() = all.first()

    fun find(id: String): QuranReciter? = all.find { it.id == id }
}
