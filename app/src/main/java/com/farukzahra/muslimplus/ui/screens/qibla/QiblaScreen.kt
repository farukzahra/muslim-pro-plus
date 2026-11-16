package com.farukzahra.muslimplus.ui.screens.qibla

import android.content.Context
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farukzahra.muslimplus.R
import com.farukzahra.muslimplus.qibla.CompassAccuracy
import com.farukzahra.muslimplus.qibla.QiblaCompass
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QiblaScreen(viewModel: QiblaViewModel = viewModel()) {
    val state = viewModel.state.collectAsState().value ?: return
    val rotation = rememberDisplayRotation()
    LaunchedEffect(rotation) { viewModel.onDisplayRotation(rotation) }
    val heading = state.heading?.toDouble()
    val facing = QiblaCompass.isFacing(heading, state.bearing)
    var showHelp by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = state.city.name,
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${state.bearing.roundToInt()}°",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.primary,
        )
        Spacer(Modifier.height(16.dp))

        Compass(
            heading = heading,
            bearing = state.bearing,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
                .testTag("qibla_compass"),
        )

        if (facing) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .background(scheme.primary.copy(alpha = 0.14f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("qibla_facing"),
            ) {
                Text(
                    text = stringResource(R.string.qibla_facing),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.primary,
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.qibla_distance, state.distanceKm.roundToInt()),
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (state.sensorAvailable) {
                stringResource(R.string.qibla_calibration)
            } else {
                stringResource(R.string.qibla_no_sensor)
            },
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )

        when {
            state.accuracy == CompassAccuracy.MEDIUM -> {
                Spacer(Modifier.height(10.dp))
                AccuracyCard(
                    text = stringResource(R.string.qibla_accuracy_medium),
                    color = scheme.secondary,
                )
            }

            QiblaCompass.interference(state.accuracy) -> {
                Spacer(Modifier.height(10.dp))
                AccuracyCard(
                    text = stringResource(R.string.qibla_interference),
                    color = scheme.error,
                    modifier = Modifier.testTag("qibla_interference"),
                )
            }
        }

        TextButton(
            onClick = { showHelp = true },
            modifier = Modifier
                .padding(top = 8.dp)
                .testTag("qibla_help"),
        ) {
            Text(stringResource(R.string.qibla_help))
        }
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(stringResource(R.string.qibla_help_title)) },
            text = { Text(stringResource(R.string.qibla_help_body)) },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
        )
    }
}

/**
 * Giro do display: o topo da tela muda de eixo quando o aparelho deita.
 * A configuração entra como chave para acompanhar a rotação.
 */
@Composable
private fun rememberDisplayRotation(): Int {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay
            .rotation
    }
}

@Composable
private fun AccuracyCard(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .height(8.dp)
                .background(color, RoundedCornerShape(50))
                .padding(horizontal = 4.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

/** Cores do mostrador, com padrão do tema e troca livre nos testes. */
internal data class CompassPalette(
    val background: Color,
    val ring: Color,
    val needle: Color,
    val north: Color,
    val label: Color,
)

@Composable
internal fun compassPalette(): CompassPalette {
    val scheme = MaterialTheme.colorScheme
    return CompassPalette(
        background = scheme.surface,
        ring = scheme.outlineVariant,
        needle = scheme.primary,
        north = scheme.error,
        label = scheme.onSurfaceVariant,
    )
}

/**
 * Bússola da Qibla.
 *
 * O mostrador gira com o rumo do aparelho, como uma bússola de verdade: o
 * "N" aponta para o norte e a ponta da agulha aponta para Makkah na tela, do
 * jeito que o usuário vê o mundo. Com o aparelho virado para Makkah a agulha
 * fica no topo e o "N" vai para o lado, o que deixa a leitura do ângulo
 * (ex.: 69°) sem ambiguidade.
 *
 * Sem bússola ([heading] nulo) o mostrador fica parado com o norte no topo,
 * que é o modo "apenas o ângulo a partir do norte verdadeiro".
 */
@Composable
internal fun Compass(
    heading: Double?,
    bearing: Double,
    modifier: Modifier = Modifier,
    palette: CompassPalette = compassPalette(),
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelLarge
        .copy(fontWeight = FontWeight.SemiBold)
    val cardinal = listOf(
        0.0 to stringResource(R.string.qibla_compass_north),
        90.0 to stringResource(R.string.qibla_compass_east),
        180.0 to stringResource(R.string.qibla_compass_south),
        270.0 to stringResource(R.string.qibla_compass_west),
    )
    val dial = heading?.let { QiblaCompass.dialRotation(it) } ?: 0.0
    val marker = heading?.let { QiblaCompass.qiblaRotation(bearing, it) }
        ?: QiblaCompass.normalize(bearing)

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(color = palette.background, radius = radius, center = center)
        drawCircle(color = palette.ring, radius = radius, center = center, style = Stroke(width = 3f))

        rotate(degrees = dial.toFloat(), pivot = center) {
            for (i in 0 until 72) {
                val long = i % 6 == 0
                val inner = radius * if (long) 0.86f else 0.92f
                val outer = radius * 0.98f
                drawLine(
                    color = palette.ring,
                    start = pointAt(center, inner, i * 5.0),
                    end = pointAt(center, outer, i * 5.0),
                    strokeWidth = if (long) 4f else 2f,
                )
            }
            val northMark = Path().apply {
                val tip = pointAt(center, radius * 0.965f, 0.0)
                val left = pointAt(center, radius * 0.90f, -3.2)
                val right = pointAt(center, radius * 0.90f, 3.2)
                moveTo(tip.x, tip.y)
                lineTo(left.x, left.y)
                lineTo(right.x, right.y)
                close()
            }
            drawPath(northMark, color = palette.north)
        }

        // As letras ficam em pé: só a posição acompanha o giro do mostrador.
        for ((angle, text) in cardinal) {
            val style: TextStyle = labelStyle.copy(
                color = if (angle == 0.0) palette.north else palette.label,
            )
            val layout = measurer.measure(text, style)
            val spot = pointAt(center, radius * 0.70f, angle + dial)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    spot.x - layout.size.width / 2f,
                    spot.y - layout.size.height / 2f,
                ),
            )
        }

        rotate(degrees = marker.toFloat(), pivot = center) {
            val needle = Path().apply {
                moveTo(center.x, center.y - radius * 0.80f)
                lineTo(center.x - radius * 0.075f, center.y - radius * 0.03f)
                lineTo(center.x, center.y + radius * 0.22f)
                lineTo(center.x + radius * 0.075f, center.y - radius * 0.03f)
                close()
            }
            drawPath(needle, color = palette.needle)
        }

        drawCircle(color = palette.needle, radius = radius * 0.045f, center = center)
    }
}

/** Ponto do círculo a [clockwiseDegrees] do topo (0° = topo, 90° = direita). */
private fun pointAt(center: Offset, radius: Float, clockwiseDegrees: Double): Offset {
    val angle = Math.toRadians(clockwiseDegrees)
    return Offset(
        center.x + (radius * sin(angle)).toFloat(),
        center.y - (radius * cos(angle)).toFloat(),
    )
}
