package com.farukzahra.muslimplus

import android.hardware.SensorManager
import com.farukzahra.muslimplus.qibla.CompassAccuracy
import com.farukzahra.muslimplus.qibla.QiblaCompass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
}
