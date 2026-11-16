package com.farukzahra.muslimplus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.farukzahra.muslimplus.ui.screens.qibla.Compass
import com.farukzahra.muslimplus.ui.screens.qibla.CompassPalette
import kotlin.math.abs
import kotlin.math.atan2
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A agulha e a rosa são medidas no bitmap renderizado: é a verificação de que
 * a Qibla aparece no ângulo certo na tela, e não só na conta.
 */
@RunWith(AndroidJUnit4::class)
class QiblaCompassRenderTest {
    @get:Rule val compose = createComposeRule()

    private val palette = CompassPalette(
        background = Color.Black,
        ring = Color(0xFF202020),
        needle = Color.Red,
        north = Color.Blue,
        label = Color.Gray,
    )

    @Test
    fun needleSitsAtTheBearingWhenThePhoneFacesNorth() {
        render(heading = 0.0, bearing = 69.0)
        val pixels = capture()
        assertAngle(69.0, angleOf(pixels, Color.Red), 3.0)
        assertAngle(0.0, angleOf(pixels, Color.Blue), 5.0)
    }

    @Test
    fun facingMakkahPutsTheNeedleOnTopAndNorthOnTheSide() {
        render(heading = 69.0, bearing = 69.0)
        val pixels = capture()
        assertAngle(0.0, angleOf(pixels, Color.Red), 3.0)
        assertAngle(291.0, angleOf(pixels, Color.Blue), 5.0)
    }

    @Test
    fun turningEastMovesTheNeedleLeftAndNorthRight() {
        render(heading = 90.0, bearing = 69.0)
        val pixels = capture()
        assertAngle(339.0, angleOf(pixels, Color.Red), 3.0)
        assertAngle(270.0, angleOf(pixels, Color.Blue), 5.0)
    }

    /** Compara em círculo: 359,8° está a 0,2° de 0°. */
    private fun assertAngle(expected: Double, actual: Double, tolerance: Double) {
        val diff = abs(((actual - expected + 540.0) % 360.0) - 180.0)
        assertTrue("esperado $expected°, medido $actual°", diff <= tolerance)
    }

    private fun render(heading: Double, bearing: Double) {
        compose.setContent {
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .background(Color.Black)
                    .testTag(DIAL),
            ) {
                Compass(
                    heading = heading,
                    bearing = bearing,
                    modifier = Modifier.fillMaxSize(),
                    palette = palette,
                )
            }
        }
        compose.waitForIdle()
    }

    private fun capture(): PixelMap =
        compose.onNodeWithTag(DIAL).captureToImage().toPixelMap()

    /** Direção do centróide dos pixels da cor, em graus a partir do topo. */
    private fun angleOf(pixels: PixelMap, color: Color): Double {
        val target = color.toArgb()
        var sumX = 0.0
        var sumY = 0.0
        var count = 0
        for (x in 0 until pixels.width) {
            for (y in 0 until pixels.height) {
                if (closeEnough(pixels[x, y].toArgb(), target)) {
                    sumX += x
                    sumY += y
                    count++
                }
            }
        }
        assertTrue("cor $color não apareceu no mostrador", count > 50)
        val dx = sumX / count - pixels.width / 2.0
        val dy = sumY / count - pixels.height / 2.0
        val degrees = Math.toDegrees(atan2(dx, -dy))
        return ((degrees % 360.0) + 360.0) % 360.0
    }

    /** Antialias mistura a cor com o fundo; o miolo do traço continua puro. */
    private fun closeEnough(pixel: Int, target: Int): Boolean =
        abs((pixel shr 16 and 0xFF) - (target shr 16 and 0xFF)) <= 12 &&
            abs((pixel shr 8 and 0xFF) - (target shr 8 and 0xFF)) <= 12 &&
            abs((pixel and 0xFF) - (target and 0xFF)) <= 12

    private companion object {
        const val DIAL = "qibla_dial"
    }
}
