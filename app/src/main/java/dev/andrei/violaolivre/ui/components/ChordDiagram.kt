package dev.andrei.violaolivre.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.andrei.violaolivre.music.ChordShape

private const val FRETS_SHOWN = 5

/**
 * Diagrama de acorde tradicional (vertical): cordas em pé, da 6ª (esquerda) à 1ª (direita),
 * casas de cima para baixo. Bolinhas com o número do dedo, X = corda abafada, O = solta.
 * Em acordes mais acima no braço, o número à esquerda indica a casa da primeira linha.
 */
@Composable
fun ChordDiagram(shape: ChordShape, modifier: Modifier = Modifier, width: Dp = 96.dp) {
    val textMeasurer = rememberTextMeasurer()
    val colors = MaterialTheme.colorScheme
    val lineColor = colors.onSurfaceVariant
    val dotColor = colors.primary
    val onDot = colors.onPrimary
    val markerColor = colors.onSurface
    val scale = width / 96.dp // os tamanhos de texto acompanham o tamanho do diagrama

    Canvas(modifier.size(width, width * 1.25f)) {
        val labelWidth = size.width * 0.16f  // coluna do número da casa
        val markerHeight = size.height * 0.13f // linha do X / O
        val gridLeft = labelWidth + size.width * 0.04f
        val gridRight = size.width - size.width * 0.06f
        val gridTop = markerHeight + size.height * 0.03f
        val gridBottom = size.height - size.height * 0.03f
        val stringGap = (gridRight - gridLeft) / 5
        val fretGap = (gridBottom - gridTop) / FRETS_SHOWN
        val radius = minOf(stringGap, fretGap) * 0.38f
        val base = shape.baseFret
        fun stringX(string: Int) = gridLeft + string * stringGap
        fun fretY(fret: Int) = gridTop + (fret - base + 0.5f) * fretGap

        // Linhas das casas; a de cima é a pestana (grossa) quando o acorde começa na casa 1
        for (line in 0..FRETS_SHOWN) {
            val y = gridTop + line * fretGap
            val thickness = if (line == 0 && base == 1) 5.dp.toPx() else 1.5.dp.toPx()
            drawLine(lineColor, Offset(gridLeft, y), Offset(gridRight, y), strokeWidth = thickness)
        }
        // Cordas
        for (string in 0..5) {
            drawLine(lineColor, Offset(stringX(string), gridTop), Offset(stringX(string), gridBottom), strokeWidth = 1.5.dp.toPx())
        }
        // Número da casa inicial (só fora da 1ª posição)
        if (base > 1) {
            drawCenteredText(
                textMeasurer, "${base}ª", Offset(labelWidth / 2, gridTop + fretGap / 2),
                TextStyle(color = markerColor, fontSize = 11.sp * scale),
            )
        }

        // Pestana: uma barra de ponta a ponta das cordas presas pelo indicador
        val barre = shape.barre
        if (barre != null) {
            val first = shape.frets.indexOfFirst { it == barre }
            val last = shape.frets.indexOfLast { it == barre }
            val y = fretY(barre)
            drawRoundRect(
                dotColor,
                topLeft = Offset(stringX(first) - radius, y - radius),
                size = Size(stringX(last) - stringX(first) + 2 * radius, 2 * radius),
                cornerRadius = CornerRadius(radius),
            )
            drawCenteredText(textMeasurer, "1", Offset(stringX(first), y), fingerStyle(onDot, scale))
        }

        shape.frets.forEachIndexed { string, fret ->
            val x = stringX(string)
            when {
                fret < 0 -> drawCenteredText(textMeasurer, "×", Offset(x, markerHeight / 2), markerStyle(markerColor, scale))
                fret == 0 -> drawCenteredText(textMeasurer, "○", Offset(x, markerHeight / 2), markerStyle(markerColor, scale))
                fret == barre -> Unit // já desenhada na barra da pestana
                else -> {
                    val center = Offset(x, fretY(fret))
                    drawCircle(dotColor, radius, center)
                    drawCenteredText(textMeasurer, shape.fingers[string].toString(), center, fingerStyle(onDot, scale))
                }
            }
        }
    }
}

private fun markerStyle(color: Color, scale: Float) =
    TextStyle(color = color, fontSize = 12.sp * scale, fontWeight = FontWeight.Bold)

private fun fingerStyle(color: Color, scale: Float) =
    TextStyle(color = color, fontSize = 10.sp * scale, fontWeight = FontWeight.Bold)
