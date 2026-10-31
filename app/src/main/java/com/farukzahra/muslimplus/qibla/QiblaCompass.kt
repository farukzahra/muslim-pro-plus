package com.farukzahra.muslimplus.qibla

/** Precisão da bússola, traduzida do `SensorManager` para algo que a tela usa. */
enum class CompassAccuracy {
    UNKNOWN,
    HIGH,
    MEDIUM,
    LOW,
    UNRELIABLE,
    ;

    /** Só vale a pena avisar quando a leitura pode enganar. */
    val needsAttention: Boolean
        get() = this == LOW || this == UNRELIABLE

    companion object {
        fun fromSensor(status: Int): CompassAccuracy = when (status) {
            android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> HIGH
            android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> MEDIUM
            android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW -> LOW
            android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE -> UNRELIABLE
            else -> UNKNOWN
        }
    }
}

/**
 * Regras puras da bússola: quando avisar de interferência e quando considerar
 * que o aparelho está de frente para a Caaba.
 */
object QiblaCompass {
    /** Tolerância generosa: a agulha do celular não é instrumento de precisão. */
    const val FACING_TOLERANCE_DEGREES = 5.0

    fun interference(accuracy: CompassAccuracy): Boolean = accuracy.needsAttention

    /**
     * Diferença angular entre dois rumos, sempre entre 0° e 180°. O cálculo
     * fecha o círculo em 360°, então 359° e 1° ficam a 2°.
     */
    fun angularDistance(a: Double, b: Double): Double {
        val diff = ((a - b) % 360.0 + 360.0) % 360.0
        return if (diff > 180.0) 360.0 - diff else diff
    }

    fun isFacing(bearing: Double?, target: Double): Boolean {
        val value = bearing ?: return false
        return angularDistance(value, target) <= FACING_TOLERANCE_DEGREES
    }
}
