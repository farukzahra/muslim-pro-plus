package com.farukzahra.muslimplus.ui.screens.qibla

import android.app.Application
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.farukzahra.muslimplus.ServiceLocator
import com.farukzahra.muslimplus.location.City
import com.farukzahra.muslimplus.qibla.CompassAccuracy
import com.farukzahra.muslimplus.qibla.QiblaMath
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class QiblaUiState(
    val city: City,
    val bearing: Double,
    val distanceKm: Double,
    val azimuth: Float?,
    val sensorAvailable: Boolean,
    val accuracy: CompassAccuracy = CompassAccuracy.UNKNOWN,
)

/** Leitura do sensor: rumo magnético e precisão informada pelo sistema. */
private data class SensorSample(
    val azimuth: Float,
    val accuracy: CompassAccuracy,
)

class QiblaViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorManager = application.getSystemService(SensorManager::class.java)
    private val settings = ServiceLocator.settings

    private val _state = MutableStateFlow<QiblaUiState?>(null)
    val state: StateFlow<QiblaUiState?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(settings.cityId, sensorFlow()) { cityId, sample -> cityId to sample }
                .collect { (cityId, sample) ->
                    val city = ServiceLocator.cities.byId(cityId)
                    _state.value = QiblaUiState(
                        city = city,
                        bearing = QiblaMath.bearingToKaaba(city.latitude, city.longitude),
                        distanceKm = QiblaMath.distanceToKaaba(city.latitude, city.longitude),
                        azimuth = sample?.azimuth,
                        sensorAvailable = sample != null ||
                            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null,
                        accuracy = sample?.accuracy ?: CompassAccuracy.UNKNOWN,
                    )
                }
        }
    }

    private fun sensorFlow() = callbackFlow<SensorSample?> {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensor == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        // Bussola nao precisa de 60 Hz: a agulha anda em passos de meio grau e
        // o resto da tela nao muda. Sem o limite o Compose fica recompondo em
        // todo evento do sensor e nunca fica ocioso (teste instrumentado nao
        // consegue sincronizar).
        var lastSentAt = 0L
        var lastAzimuth: Float? = null
        var accuracy = CompassAccuracy.UNKNOWN
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val now = SystemClock.elapsedRealtime()
                if (now - lastSentAt < SENSOR_INTERVAL_MS) return
                val rotation = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotation, orientation)
                val magnetic = (Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0
                val azimuth = Math.round(magnetic * 2.0).toFloat() / 2f
                if (azimuth == lastAzimuth && accuracy == CompassAccuracy.UNKNOWN) return
                lastSentAt = now
                lastAzimuth = azimuth
                trySend(SensorSample(azimuth, accuracy))
            }

            override fun onAccuracyChanged(sensor: Sensor?, status: Int) {
                accuracy = CompassAccuracy.fromSensor(status)
                // Repinta na hora: o usuário precisa ver o aviso de
                // interferência mesmo com a agulha parada.
                lastAzimuth?.let { trySend(SensorSample(it, accuracy)) }
            }
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    /** Converte rumo magnetico em verdadeiro usando a declinacao local. */
    fun declination(city: City): Float {
        val field = GeomagneticField(
            city.latitude.toFloat(),
            city.longitude.toFloat(),
            0f,
            System.currentTimeMillis(),
        )
        return field.declination
    }

    private companion object {
        /** Intervalo minimo entre duas atualizacoes da agulha. */
        const val SENSOR_INTERVAL_MS = 80L
    }
}
