package com.farukzahra.muslimplus

import com.farukzahra.muslimplus.qibla.QiblaMath
import org.junit.Assert.assertEquals
import org.junit.Test

class QiblaMathTest {
    @Test
    fun bearingFromSaoPaulo() {
        val bearing = QiblaMath.bearingToKaaba(-23.5505, -46.6333)
        assertEquals(69.0, bearing, 1.5)
    }

    @Test
    fun bearingFromLondon() {
        val bearing = QiblaMath.bearingToKaaba(51.5074, -0.1278)
        assertEquals(119.0, bearing, 1.5)
    }

    @Test
    fun distanceFromSaoPauloIsPlausible() {
        val distance = QiblaMath.distanceToKaaba(-23.5505, -46.6333)
        assertEquals(10_500.0, distance, 500.0)
    }
}
