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
import com.farukzahra.muslimplus.qibla.QiblaCompass
import com.farukzahra.muslimplus.qibla.QiblaMath
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
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
    /** Rumo verdadeiro do topo da tela; nulo sem bússola. */
    val heading: Float?,
    val sensorAvailable: Boolean,
    val accuracy: CompassAccuracy = CompassAccuracy.UNKNOWN,
)

/** Leitura crua dos sensores: gravidade, campo magnético e precisão. */
private class SensorReading(
    val gravity: FloatArray,
    val magnetic: FloatArray,
    val accuracy: CompassAccuracy,
)

class QiblaViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorManager = application.getSystemService(SensorManager::class.java)
    private val settings = ServiceLocator.settings

    /** Giro da tela: muda o eixo que aponta "para frente". */
    private val displayRotation =
        MutableStateFlow(QiblaCompass.DISPLAY_ROTATION_0)

    private val hasCompass = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null &&
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null

    private val _state = MutableStateFlow<QiblaUiState?>(null)
    val state: StateFlow<QiblaUiState?> = _state.asStateFlow()

    /** Última declinação calculada: só muda quando a cidade muda. */
    private var declinationCache: Pair<String, Float>? = null

    init {
        viewModelScope.launch {
            combine(settings.cityId, readings(), displayRotation) { cityId, reading, rotation ->
                Triple(cityId, reading, rotation)
            }.collect { (cityId, reading, rotation) ->
                val city = ServiceLocator.cities.byId(cityId)
                val magnetic = reading?.let {
                    QiblaCompass.headingDegrees(it.gravity, it.magnetic, rotation)
                }
                val heading = magnetic?.let {
                    QiblaCompass.trueHeading(it, declinationFor(city).toDouble())
                }
                _state.value = QiblaUiState(
                    city = city,
                    bearing = QiblaMath.bearingToKaaba(city.latitude, city.longitude),
                    distanceKm = QiblaMath.distanceToKaaba(city.latitude, city.longitude),
                    heading = heading?.let { roundToHalfDegree(it).toFloat() },
                    sensorAvailable = hasCompass,
                    accuracy = reading?.accuracy ?: CompassAccuracy.UNKNOWN,
                )
            }
        }
    }

    /** Chamado pela tela a cada mudança de orientação do display. */
    fun onDisplayRotation(rotation: Int) {
        if (rotation != displayRotation.value) displayRotation.value = rotation
    }

    private fun readings(): Flow<SensorReading?> = callbackFlow {
        val gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (gravity == null || magnetic == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        var gravityValues: FloatArray? = null
        var magneticValues: FloatArray? = null
        var accuracy = CompassAccuracy.UNKNOWN
        // A bussola nao precisa de 60 Hz: a agulha anda em passos de meio grau
        // e o resto da tela nao muda. Sem o limite o Compose fica recompondo em
        // todo evento do sensor e nunca fica ocioso (teste instrumentado nao
        // consegue sincronizar). Estado igual ja e descartado pelo StateFlow.
        var lastSentAt = 0L
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_GRAVITY -> gravityValues = event.values.copyOf(3)
                    Sensor.TYPE_MAGNETIC_FIELD -> magneticValues = event.values.copyOf(3)
                }
                val gravity = gravityValues ?: return
                val field = magneticValues ?: return
                val now = SystemClock.elapsedRealtime()
                if (now - lastSentAt < SENSOR_INTERVAL_MS) return
                lastSentAt = now
                // Sem direcao utilizavel nao ha o que mostrar: mantem a ultima.
                if (QiblaCompass.headingDegrees(gravity, field) == null) return
                trySend(SensorReading(gravity, field, accuracy))
            }

            override fun onAccuracyChanged(sensor: Sensor?, status: Int) {
                accuracy = CompassAccuracy.fromSensor(status)
                // Repinta na hora: o usuário precisa ver o aviso de
                // interferência mesmo com a agulha parada.
                val gravity = gravityValues ?: return
                val field = magneticValues ?: return
                lastSentAt = 0L
                trySend(SensorReading(gravity, field, accuracy))
            }
        }
        sensorManager.registerListener(listener, gravity, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(listener, magnetic, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    /** Converte rumo magnetico em verdadeiro usando a declinacao local. */
    private fun declinationFor(city: City): Float {
        val cached = declinationCache
        if (cached != null && cached.first == city.id) return cached.second
        val field = GeomagneticField(
            city.latitude.toFloat(),
            city.longitude.toFloat(),
            0f,
            System.currentTimeMillis(),
        )
        val value = field.declination
        declinationCache = city.id to value
        return value
    }

    private companion object {
        /** Intervalo minimo entre duas atualizacoes da agulha. */
        const val SENSOR_INTERVAL_MS = 80L
    }
}

/** Meio grau ja e mais fino do que a precisao real da bussola. */
internal fun roundToHalfDegree(degrees: Double): Double =
    Math.round(degrees * 2.0) / 2.0
