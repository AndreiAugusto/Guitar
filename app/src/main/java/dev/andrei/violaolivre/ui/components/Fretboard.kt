package dev.andrei.violaolivre.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.andrei.violaolivre.music.FretPosition
import dev.andrei.violaolivre.ui.theme.InTuneGreen

/** Destaque de uma marca no braço. */
enum class MarkStyle { ROOT, NOTE, FAINT }

/** Uma bolinha desenhada no braço, com um texto dentro (ex.: o nome da nota). */
data class FretMark(val position: FretPosition, val label: String, val style: MarkStyle = MarkStyle.NOTE)

// Casas com marcação no braço de verdade (a 12 tem duas bolinhas)
private val INLAY_FRETS = setOf(3, 5, 7, 9, 12, 15)

private val BoardColor = Color(0xFF3A2F26)
private val InlayColor = Color(0xFF5C4B3D)
private val FretWireColor = Color(0xFF9E9E9E)
private val NutColor = Color(0xFFE0D8C8)
private val StringColor = Color(0xFFBDBDBD)

/**
 * Braço do violão na horizontal, como numa tab: 1ª corda (Mi aguda) em cima,
 * casas crescendo para a direita. A coluna 0, antes da pestana, são as cordas soltas.
 * Rola na horizontal se não couber na tela.
 */
@Composable
fun Fretboard(
    marks: List<FretMark>,
    modifier: Modifier = Modifier,
    lastFret: Int = 12,
    selected: FretPosition? = null,
    onPositionClick: ((FretPosition) -> Unit)? = null,
) {
    val columnWidth = 44.dp
    val rowHeight = 36.dp
    val numbersHeight = 20.dp
    val textMeasurer = rememberTextMeasurer()
    val colors = MaterialTheme.colorScheme
    val markColors = mapOf(
        MarkStyle.ROOT to (colors.primary to colors.onPrimary),
        MarkStyle.NOTE to (Color(0xFF4A4A4E) to colors.onSurface),
        MarkStyle.FAINT to (Color(0xFF4A4A4E).copy(alpha = 0.45f) to colors.onSurface.copy(alpha = 0.45f)),
    )
    val numberColor = colors.onSurfaceVariant

    val tapModifier = if (onPositionClick == null) {
        Modifier
    } else {
        Modifier.pointerInput(lastFret) {
            detectTapGestures { offset ->
                val fret = (offset.x / columnWidth.toPx()).toInt()
                val row = (offset.y / rowHeight.toPx()).toInt()
                if (fret in 0..lastFret && row in 0..5) onPositionClick(FretPosition(string = 5 - row, fret = fret))
            }
        }
    }

    Box(modifier.horizontalScroll(rememberScrollState())) {
        Canvas(
            Modifier
                .size(columnWidth * (lastFret + 1), rowHeight * 6 + numbersHeight)
                .then(tapModifier),
        ) {
            val cw = columnWidth.toPx()
            val rh = rowHeight.toPx()
            val top = rh * 0.5f            // linha da 1ª corda
            val bottom = rh * 5.5f         // linha da 6ª corda
            val right = cw * (lastFret + 1)

            // Madeira do braço (da pestana em diante)
            drawRect(BoardColor, topLeft = Offset(cw, top - rh * 0.3f), size = Size(right - cw, bottom - top + rh * 0.6f))

            // Marcações das casas 3, 5, 7, 9, 12, 15
            INLAY_FRETS.filter { it <= lastFret }.forEach { fret ->
                val x = cw * (fret + 0.5f)
                val ys = if (fret == 12) listOf(rh * 2f, rh * 4f) else listOf(rh * 3f)
                ys.forEach { drawCircle(InlayColor, radius = rh * 0.16f, center = Offset(x, it)) }
            }

            // Trastes (o traste da casa N fica na borda direita da coluna N) e pestana
            for (fret in 1..lastFret) {
                val x = cw * (fret + 1)
                drawLine(FretWireColor, Offset(x, top - rh * 0.3f), Offset(x, bottom + rh * 0.3f), strokeWidth = 2.dp.toPx())
            }
            drawLine(NutColor, Offset(cw, top - rh * 0.3f), Offset(cw, bottom + rh * 0.3f), strokeWidth = 6.dp.toPx())

            // Cordas: as graves (embaixo) são mais grossas
            for (row in 0..5) {
                val y = rh * (row + 0.5f)
                drawLine(StringColor, Offset(cw * 0.5f, y), Offset(right, y), strokeWidth = (1f + row * 0.4f).dp.toPx())
            }

            // Números das casas
            for (fret in 1..lastFret) {
                drawCenteredText(
                    textMeasurer,
                    fret.toString(),
                    Offset(cw * (fret + 0.5f), rh * 6f + numbersHeight.toPx() / 2),
                    TextStyle(
                        color = numberColor,
                        fontSize = 11.sp,
                        fontWeight = if (fret in INLAY_FRETS) FontWeight.Bold else FontWeight.Normal,
                    ),
                )
            }

            // Bolinhas das notas
            marks.forEach { mark ->
                val center = Offset(cw * (mark.position.fret + 0.5f), rh * (5 - mark.position.string + 0.5f))
                val (background, text) = markColors.getValue(mark.style)
                drawCircle(background, radius = rh * 0.42f, center = center)
                drawCenteredText(textMeasurer, mark.label, center, TextStyle(color = text, fontSize = 12.sp, fontWeight = FontWeight.Medium))
            }

            // Anel na posição tocada
            selected?.let {
                val center = Offset(cw * (it.fret + 0.5f), rh * (5 - it.string + 0.5f))
                drawCircle(InTuneGreen, radius = rh * 0.46f, center = center, style = Stroke(width = 3.dp.toPx()))
            }
        }
    }
}
