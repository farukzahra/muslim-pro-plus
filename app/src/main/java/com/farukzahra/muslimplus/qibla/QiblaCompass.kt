package com.farukzahra.muslimplus.qibla

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

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
 * Regras puras da bússola: rumo a partir dos sensores, giro do mostrador,
 * quando avisar de interferência e quando o aparelho está de frente para a
 * Caaba.
 *
 * O rumo vem do vetor de gravidade e do campo magnético, não de
 * `SensorManager.getOrientation`. A decomposição em ângulos tem singularidade
 * quando o aparelho fica na vertical (é exatamente como se segura o celular
 * para mirar a Qibla), e ali o rumo pula dezenas de graus. Com os vetores, a
 * conta vale em qualquer inclinação.
 *
 * A direção "para frente" da tela é o topo com o aparelho deitado e a câmera
 * traseira com o aparelho em pé. A troca acontece a 45°, onde as duas apontam
 * para o mesmo lado, então a agulha não salta.
 */
object QiblaCompass {
    /** Tolerância generosa: a agulha do celular não é instrumento de precisão. */
    const val FACING_TOLERANCE_DEGREES = 5.0

    /** Valores de `android.view.Surface.ROTATION_*`, sem depender da classe. */
    const val DISPLAY_ROTATION_0 = 0
    const val DISPLAY_ROTATION_90 = 1
    const val DISPLAY_ROTATION_180 = 2
    const val DISPLAY_ROTATION_270 = 3

    fun interference(accuracy: CompassAccuracy): Boolean = accuracy.needsAttention

    /** Fecha qualquer ângulo no intervalo [0, 360). */
    fun normalize(degrees: Double): Double = ((degrees % 360.0) + 360.0) % 360.0

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

    /**
     * Rumo verdadeiro: o magnético corrigido pela declinação local.
     * `GeomagneticField.declination` é positiva a leste do norte verdadeiro.
     */
    fun trueHeading(magneticHeading: Double, declination: Double): Double =
        normalize(magneticHeading + declination)

    /**
     * Giro do mostrador para o norte do desenho apontar para o norte real.
     * A tela gira o sentido horário, então o mostrador gira ao contrário.
     */
    fun dialRotation(trueHeading: Double): Double = normalize(-trueHeading)

    /**
     * Ângulo da agulha da Caaba no quadro da tela: rumo da Caaba menos o rumo
     * do aparelho. A agulha fica no topo quando o aparelho aponta para Makkah.
     */
    fun qiblaRotation(bearing: Double, trueHeading: Double): Double =
        normalize(bearing - trueHeading)

    /**
     * Rumo magnético do "para frente" da tela, em graus, 0 = norte magnético e
     * crescendo no sentido horário. `null` quando não há campo utilizável
     * (aparelho parado na vertical do polo ou sensor sem leitura).
     */
    fun headingDegrees(
        gravity: FloatArray,
        magnetic: FloatArray,
        displayRotation: Int = DISPLAY_ROTATION_0,
    ): Double? {
        if (gravity.size < 3 || magnetic.size < 3) return null
        // O sensor de gravidade aponta para cima: é a reação ao peso, não a
        // queda. Deitado com a tela para o céu ele lê +Z.
        val up = unit(gravity[0], gravity[1], gravity[2]) ?: return null
        val field = unit(magnetic[0], magnetic[1], magnetic[2]) ?: return null
        // Norte = parte horizontal do campo magnético.
        val vertical = dot(field, up)
        val north = unit(
            field[0] - vertical * up[0],
            field[1] - vertical * up[1],
            field[2] - vertical * up[2],
        ) ?: return null
        val east = cross(north, up)
        val forward = forwardAxis(displayRotation, up)
        val degrees = Math.toDegrees(
            atan2(dot(forward, east).toDouble(), dot(forward, north).toDouble()),
        )
        return normalize(degrees)
    }

    /** Topo da tela nos eixos do aparelho, conforme o giro do display. */
    fun screenForward(displayRotation: Int): FloatArray = when (displayRotation) {
        DISPLAY_ROTATION_90 -> floatArrayOf(1f, 0f, 0f)
        DISPLAY_ROTATION_180 -> floatArrayOf(0f, -1f, 0f)
        DISPLAY_ROTATION_270 -> floatArrayOf(-1f, 0f, 0f)
        else -> floatArrayOf(0f, 1f, 0f)
    }

    /**
     * Eixo que aponta para o horizonte: o topo da tela ou a câmera traseira,
     * o que estiver mais deitado. O outro fica vertical e não define rumo.
     */
    private fun forwardAxis(displayRotation: Int, up: FloatArray): FloatArray {
        val screen = screenForward(displayRotation)
        val camera = floatArrayOf(0f, 0f, -1f)
        return if (abs(dot(camera, up)) < abs(dot(screen, up))) camera else screen
    }

    private fun unit(x: Float, y: Float, z: Float): FloatArray? {
        val length = sqrt(x * x + y * y + z * z)
        if (length < MIN_VECTOR_LENGTH) return null
        return floatArrayOf(x / length, y / length, z / length)
    }

    private fun dot(a: FloatArray, b: FloatArray): Float =
        a[0] * b[0] + a[1] * b[1] + a[2] * b[2]

    private fun cross(a: FloatArray, b: FloatArray): FloatArray = floatArrayOf(
        a[1] * b[2] - a[2] * b[1],
        a[2] * b[0] - a[0] * b[2],
        a[0] * b[1] - a[1] * b[0],
    )

    /** Abaixo disto o vetor é ruído: não dá para tirar direção dele. */
    private const val MIN_VECTOR_LENGTH = 1e-3f
}
