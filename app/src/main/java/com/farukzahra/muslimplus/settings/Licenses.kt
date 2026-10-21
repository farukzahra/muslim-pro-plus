package com.farukzahra.muslimplus.settings

import com.farukzahra.muslimplus.R

/**
 * Uma atribuicao da tela de Licencas: quem forneceu, sob qual licenca e onde
 * conferir os termos.
 *
 * [noteRes] e o texto curto que explica para que o dado/componente e usado
 * (traduzido); nome, licenca e URL sao nomes proprios e ficam iguais nos tres
 * idiomas de proposito.
 */
data class LicenseEntry(
    val name: String,
    val license: String,
    val url: String,
    val noteRes: Int? = null,
)

/**
 * Fonte unica das atribuicoes mostradas em Licencas. Fica fora das telas para
 * o teste JVM conseguir checar a lista sem Compose.
 *
 * Toda URL precisa ser `https://` (o app nao abre `http://`) e toda entrada
 * precisa de nome e licenca preenchidos — `LicensesTest` quebra o build caso
 * contrario.
 */
object Licenses {
    const val TANZIL_URL = "https://tanzil.net/docs/tanzil_project"
    const val GEONAMES_URL = "https://www.geonames.org/"
    const val ADHAN_URL = "https://github.com/batoulapps/adhan-java"
    const val MEDIA3_URL = "https://github.com/androidx/media"
    const val EVERYAYAH_URL = "https://everyayah.com/"
    const val QURAN_API_URL = "https://quran.com/"

    val all: List<LicenseEntry> = listOf(
        LicenseEntry(
            name = "Tanzil",
            license = "CC BY 3.0",
            url = TANZIL_URL,
            noteRes = R.string.licenses_note_tanzil,
        ),
        LicenseEntry(
            name = "GeoNames",
            license = "CC BY 4.0",
            url = GEONAMES_URL,
            noteRes = R.string.licenses_note_geonames,
        ),
        LicenseEntry(
            name = "adhan-java",
            license = "MIT",
            url = ADHAN_URL,
            noteRes = R.string.licenses_note_adhan,
        ),
        LicenseEntry(
            name = "AndroidX Media3",
            license = "Apache 2.0",
            url = MEDIA3_URL,
            noteRes = R.string.licenses_note_media3,
        ),
        LicenseEntry(
            name = "everyayah.com",
            license = "Per site terms",
            url = EVERYAYAH_URL,
            noteRes = R.string.licenses_note_everyayah,
        ),
        LicenseEntry(
            name = "Helmi Nasr",
            license = "Quran.com API",
            url = QURAN_API_URL,
            noteRes = R.string.licenses_note_helmi_nasr,
        ),
        LicenseEntry(
            name = "Pickthall",
            license = "Quran.com API",
            url = QURAN_API_URL,
            noteRes = R.string.licenses_note_pickthall,
        ),
    )
}
