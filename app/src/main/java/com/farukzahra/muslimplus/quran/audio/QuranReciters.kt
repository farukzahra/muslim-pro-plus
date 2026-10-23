package com.farukzahra.muslimplus.quran.audio

import com.farukzahra.muslimplus.quran.VerseRef

/** Curated Arabic Hafs recordings from EveryAyah's recitations.js (2026-10-23). */
data class QuranReciter(
    override val id: String,
    override val reciter: String,
    val folder: String,
    val bitrateKbps: Int,
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
    val all: List<QuranReciter> = listOf(
        QuranReciter("alafasy-128", "Mishary Alafasy", "Alafasy_128kbps", 128),
        QuranReciter("sudais-192", "Abdurrahmaan As-Sudais", "Abdurrahmaan_As-Sudais_192kbps", 192),
        QuranReciter("husary-128", "Mahmoud Khalil Al-Husary", "Husary_128kbps", 128),
        QuranReciter("shaatree-128", "Abu Bakr Ash-Shaatree", "Abu_Bakr_Ash-Shaatree_128kbps", 128),
    )
    val default: QuranReciter get() = all.first()
    fun find(id: String): QuranReciter? = all.find { it.id == id }
}

