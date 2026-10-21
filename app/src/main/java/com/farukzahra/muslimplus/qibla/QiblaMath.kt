package com.farukzahra.muslimplus.qibla

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object QiblaMath {
    const val KAABA_LAT = 21.4225
    const val KAABA_LON = 39.8262
    private const val EARTH_RADIUS_KM = 6371.0088

    /** Rumo inicial (graus, norte verdadeiro) do ponto ate a Caaba. */
    fun bearingToKaaba(latitude: Double, longitude: Double): Double {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LAT)
        val deltaLon = Math.toRadians(KAABA_LON - longitude)
        val y = sin(deltaLon)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLon)
        val degrees = Math.toDegrees(atan2(y, x))
        return (degrees + 360.0) % 360.0
    }

    /** Distancia em km pela formula de haversine. */
    fun distanceToKaaba(latitude: Double, longitude: Double): Double {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LAT)
        val dPhi = phi2 - phi1
        val dLambda = Math.toRadians(KAABA_LON - longitude)
        val a = sin(dPhi / 2) * sin(dPhi / 2) +
            cos(phi1) * cos(phi2) * sin(dLambda / 2) * sin(dLambda / 2)
        return 2 * EARTH_RADIUS_KM * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    }
}
