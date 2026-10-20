package com.farukzahra.muslimplus.prayer

/**
 * Metodos de calculo com os angulos publicados. `ishaMinutes > 0` significa
 * intervalo fixo apos o maghrib (Umm al-Qura e Qatar usam 90 min).
 */
enum class PrayerMethod(
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutes: Int,
) {
    MWL(18.0, 17.0, 0),
    ISNA(15.0, 15.0, 0),
    EGYPT(19.5, 17.5, 0),
    KARACHI(18.0, 18.0, 0),
    MAKKAH(18.5, 0.0, 90),
    DUBAI(18.2, 18.2, 0),
    MOONSIGHTING(18.0, 18.0, 0),
    TURKEY(18.0, 17.0, 0),
    TEHRAN(17.7, 14.0, 0),
    KUWAIT(18.0, 17.5, 0),
    QATAR(18.0, 0.0, 90),
    SINGAPORE(20.0, 18.0, 0),
}
