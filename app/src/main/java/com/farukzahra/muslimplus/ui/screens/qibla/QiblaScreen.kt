package com.farukzahra.muslimplus.ui.screens.qibla

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farukzahra.muslimplus.R
import kotlin.math.roundToInt

@Composable
fun QiblaScreen(viewModel: QiblaViewModel = viewModel()) {
    val state = viewModel.state.collectAsState().value ?: return
    val declination = viewModel.declination(state.city)
    val trueAzimuth = state.azimuth?.let { (it + declination + 360f) % 360f }
    val needleRotation = trueAzimuth?.let { (state.bearing - it + 360.0) % 360.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = state.city.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${state.bearing.roundToInt()}°",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(20.dp))

        Compass(
            needleRotation = needleRotation,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f),
        )

        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.qibla_distance, state.distanceKm.roundToInt()),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (state.sensorAvailable) {
                stringResource(R.string.qibla_calibration)
            } else {
                stringResource(R.string.qibla_no_sensor)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Compass(needleRotation: Double?, modifier: Modifier = Modifier) {
    val ringColor = MaterialTheme.colorScheme.outlineVariant
    val needleColor = MaterialTheme.colorScheme.primary
    val northColor = MaterialTheme.colorScheme.error
    val surface = MaterialTheme.colorScheme.surface

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(color = surface, radius = radius, center = center)
        drawCircle(color = ringColor, radius = radius, center = center, style = Stroke(width = 3f))

        for (i in 0 until 72) {
            val angle = Math.toRadians(i * 5.0)
            val long = i % 6 == 0
            val inner = radius * if (long) 0.86f else 0.92f
            val outer = radius * 0.98f
            val start = Offset(
                center.x + (inner * kotlin.math.sin(angle)).toFloat(),
                center.y - (inner * kotlin.math.cos(angle)).toFloat(),
            )
            val end = Offset(
                center.x + (outer * kotlin.math.sin(angle)).toFloat(),
                center.y - (outer * kotlin.math.cos(angle)).toFloat(),
            )
            drawLine(
                color = ringColor,
                start = start,
                end = end,
                strokeWidth = if (long) 4f else 2f,
            )
        }

        if (needleRotation != null) {
            rotate(degrees = needleRotation.toFloat(), pivot = center) {
                val needle = Path().apply {
                    moveTo(center.x, center.y - radius * 0.62f)
                    lineTo(center.x - radius * 0.07f, center.y + radius * 0.18f)
                    lineTo(center.x + radius * 0.07f, center.y + radius * 0.18f)
                    close()
                }
                drawPath(needle, color = needleColor)
                drawCircle(color = needleColor, radius = radius * 0.05f, center = center)
            }
        }

        drawCircle(color = northColor, radius = 6f, center = Offset(center.x, 14f))
    }
}
