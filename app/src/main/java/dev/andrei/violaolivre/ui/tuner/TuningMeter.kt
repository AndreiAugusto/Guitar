package dev.andrei.violaolivre.ui.tuner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import dev.andrei.violaolivre.ui.theme.InTuneGreen
import dev.andrei.violaolivre.ui.theme.OutOfTuneOrange

/** Os extremos do mostrador: ±50 cents (meio semitom). */
private const val METER_RANGE_CENTS = 50.0

/**
 * Mostrador horizontal: o centro é a nota exata; o ponteiro vai para a esquerda
 * quando a corda está baixa e para a direita quando está alta.
 * [cents] null esconde o ponteiro (nenhum som); [needleAlpha] < 1 esmaece só o ponteiro.
 */
@Composable
fun TuningMeter(cents: Double?, isInTune: Boolean, modifier: Modifier = Modifier, needleAlpha: Float = 1f) {
    // Posição de -1 (esquerda) a 1 (direita), animada para o ponteiro deslizar em vez de pular
    val target = ((cents ?: 0.0) / METER_RANGE_CENTS).coerceIn(-1.0, 1.0).toFloat()
    val position by animateFloatAsState(
        targetValue = target,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "posição do ponteiro",
    )
    val needleColor by animateColorAsState(
        targetValue = if (isInTune) InTuneGreen else OutOfTuneOrange,
        label = "cor do ponteiro",
    )
    val trackColor = MaterialTheme.colorScheme.outline
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    // Canvas: desenho livre com linhas e retângulos, como num <canvas> do HTML
    Canvas(modifier.fillMaxWidth().height(48.dp)) {
        val needleWidth = 6.dp.toPx()
        val left = needleWidth          // margem para o ponteiro não ser cortado nas pontas
        val usableWidth = size.width - 2 * needleWidth
        val centerX = left + usableWidth / 2
        val centerY = size.height / 2

        // Trilho
        val trackHeight = 6.dp.toPx()
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(left, centerY - trackHeight / 2),
            size = Size(usableWidth, trackHeight),
            cornerRadius = CornerRadius(trackHeight / 2),
        )

        // Faixa verde da zona "afinada" (±5 cents)
        val zoneHalfWidth = (usableWidth / 2) * (IN_TUNE_CENTS / METER_RANGE_CENTS).toFloat()
        val zoneHeight = 14.dp.toPx()
        drawRoundRect(
            color = InTuneGreen.copy(alpha = 0.25f),
            topLeft = Offset(centerX - zoneHalfWidth, centerY - zoneHeight / 2),
            size = Size(2 * zoneHalfWidth, zoneHeight),
            cornerRadius = CornerRadius(4.dp.toPx()),
        )

        // Marcas a cada 10 cents; a do centro é mais alta
        for (i in -5..5) {
            val x = centerX + i * usableWidth / 10
            val half = (if (i == 0) 14.dp else 7.dp).toPx()
            drawLine(tickColor, Offset(x, centerY - half), Offset(x, centerY + half), strokeWidth = 2.dp.toPx())
        }

        // Ponteiro
        if (cents != null) {
            val x = centerX + position * usableWidth / 2
            drawRoundRect(
                color = needleColor.copy(alpha = needleAlpha),
                topLeft = Offset(x - needleWidth / 2, 0f),
                size = Size(needleWidth, size.height),
                cornerRadius = CornerRadius(needleWidth / 2),
            )
        }
    }
}
