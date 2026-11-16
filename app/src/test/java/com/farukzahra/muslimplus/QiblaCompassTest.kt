package com.farukzahra.muslimplus

import android.hardware.SensorManager
import com.farukzahra.muslimplus.qibla.CompassAccuracy
import com.farukzahra.muslimplus.qibla.QiblaCompass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QiblaCompassTest {
    @Test
    fun sensorStatusMapsToReadableAccuracy() {
        assertEquals(
            CompassAccuracy.HIGH,
            CompassAccuracy.fromSensor(SensorManager.SENSOR_STATUS_ACCURACY_HIGH),
        )
        assertEquals(
            CompassAccuracy.MEDIUM,
            CompassAccuracy.fromSensor(SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM),
        )
        assertEquals(
            CompassAccuracy.LOW,
            CompassAccuracy.fromSensor(SensorManager.SENSOR_STATUS_ACCURACY_LOW),
        )
        assertEquals(
            CompassAccuracy.UNRELIABLE,
            CompassAccuracy.fromSensor(SensorManager.SENSOR_STATUS_UNRELIABLE),
        )
        assertEquals(CompassAccuracy.UNKNOWN, CompassAccuracy.fromSensor(99))
    }

    @Test
    fun onlyLowAndUnreliableRaiseTheInterferenceWarning() {
        assertFalse(QiblaCompass.interference(CompassAccuracy.HIGH))
        assertFalse(QiblaCompass.interference(CompassAccuracy.MEDIUM))
        assertFalse(QiblaCompass.interference(CompassAccuracy.UNKNOWN))
        assertTrue(QiblaCompass.interference(CompassAccuracy.LOW))
        assertTrue(QiblaCompass.interference(CompassAccuracy.UNRELIABLE))
    }

    @Test
    fun angularDistanceWrapsAroundNorth() {
        assertEquals(2.0, QiblaCompass.angularDistance(359.0, 1.0), 0.001)
        assertEquals(2.0, QiblaCompass.angularDistance(1.0, 359.0), 0.001)
        assertEquals(180.0, QiblaCompass.angularDistance(0.0, 180.0), 0.001)
        assertEquals(0.0, QiblaCompass.angularDistance(45.0, 45.0), 0.001)
    }

    @Test
    fun facingUsesAFiveDegreeTolerance() {
        assertTrue(QiblaCompass.isFacing(120.0, 122.0))
        assertTrue(QiblaCompass.isFacing(120.0, 125.0))
        assertFalse(QiblaCompass.isFacing(120.0, 126.0))
        assertFalse(QiblaCompass.isFacing(null, 120.0))
        // Sem sensor o bearing é nulo; nunca deve dizer que está de frente.
        assertFalse(QiblaCompass.isFacing(null, 0.0))
    }

    @Test
    fun normalizeBringsAnyAngleIntoTheCompassRange() {
        assertEquals(0.0, QiblaCompass.normalize(0.0), 0.001)
        assertEquals(359.0, QiblaCompass.normalize(-1.0), 0.001)
        assertEquals(10.0, QiblaCompass.normalize(370.0), 0.001)
        assertEquals(180.0, QiblaCompass.normalize(-180.0), 0.001)
    }

    @Test
    fun trueHeadingAddsTheLocalDeclination() {
        // Sao Paulo: 21 graus oeste, ou seja, declinacao negativa.
        assertEquals(68.9, QiblaCompass.trueHeading(89.9, -21.0), 0.001)
        // A soma fecha a volta em 360.
        assertEquals(5.0, QiblaCompass.trueHeading(350.0, 15.0), 0.001)
    }

    @Test
    fun dialTurnsAgainstTheHeadingAndNeedlePointsAtTheBearing() {
        // Aparelho virado para o norte: norte no topo e agulha no rumo da Caaba.
        assertEquals(0.0, QiblaCompass.dialRotation(0.0), 0.001)
        assertEquals(69.0, QiblaCompass.qiblaRotation(69.0, 0.0), 0.001)
        // Aparelho virado para Makkah: agulha no topo e o norte para a esquerda.
        assertEquals(0.0, QiblaCompass.qiblaRotation(69.0, 69.0), 0.001)
        assertEquals(291.0, QiblaCompass.dialRotation(69.0), 0.001)
        // Virar 90 graus para a direita joga a agulha 90 graus para a esquerda.
        assertEquals(339.0, QiblaCompass.qiblaRotation(69.0, 90.0), 0.001)
    }

    @Test
    fun headingWithThePhoneFlatUsesTheTopOfTheScreen() {
        val gravity = floatArrayOf(0f, 0f, 9.81f)
        assertEquals(0.0, heading(gravity, floatArrayOf(0f, 22f, -40f)), 0.01)
        assertEquals(90.0, heading(gravity, floatArrayOf(-22f, 0f, -40f)), 0.01)
        assertEquals(180.0, heading(gravity, floatArrayOf(0f, -22f, -40f)), 0.01)
        assertEquals(270.0, heading(gravity, floatArrayOf(22f, 0f, -40f)), 0.01)
    }

    @Test
    fun headingWithThePhoneUprightUsesTheCamera() {
        // Em pe: topo para cima, camera traseira para o norte ou para o leste.
        val upright = floatArrayOf(0f, 9.81f, 0f)
        assertEquals(0.0, heading(upright, floatArrayOf(0f, -40f, -22f)), 0.01)
        assertEquals(90.0, heading(upright, floatArrayOf(-22f, -40f, 0f)), 0.01)
    }

    @Test
    fun headingDoesNotJumpBetweenFlatAndUpright() {
        // Inclinado 45 graus a direcao e a mesma do aparelho deitado.
        val tilted = floatArrayOf(0f, 6.94f, 6.94f)
        assertEquals(0.0, heading(tilted, floatArrayOf(0f, -12.73f, -43.83f)), 0.01)
    }

    @Test
    fun headingFollowsTheDisplayRotation() {
        val gravity = floatArrayOf(0f, 0f, 9.81f)
        val magnetic = floatArrayOf(0f, 22f, -40f)
        assertEquals(
            90.0,
            QiblaCompass.headingDegrees(gravity, magnetic, QiblaCompass.DISPLAY_ROTATION_90)!!,
            0.01,
        )
        assertEquals(
            180.0,
            QiblaCompass.headingDegrees(gravity, magnetic, QiblaCompass.DISPLAY_ROTATION_180)!!,
            0.01,
        )
        assertEquals(
            270.0,
            QiblaCompass.headingDegrees(gravity, magnetic, QiblaCompass.DISPLAY_ROTATION_270)!!,
            0.01,
        )
    }

    @Test
    fun headingIsNullWithoutAUsableField() {
        assertNull(QiblaCompass.headingDegrees(floatArrayOf(0f, 0f, 0f), floatArrayOf(0f, 22f, -40f)))
        // Campo paralelo a gravidade (polo magnetico) nao define norte.
        assertNull(QiblaCompass.headingDegrees(floatArrayOf(0f, 0f, 9.81f), floatArrayOf(0f, 0f, 40f)))
        assertNull(QiblaCompass.headingDegrees(floatArrayOf(0f, 0f, 9.81f), floatArrayOf(0f, 0f, 0f)))
    }

    private fun heading(gravity: FloatArray, magnetic: FloatArray): Double =
        QiblaCompass.headingDegrees(gravity, magnetic) ?: error("rumo nulo")
}
