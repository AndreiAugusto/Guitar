package dev.andrei.violaolivre.ui.theory.lessons

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.music.FretPosition
import dev.andrei.violaolivre.music.FretPositions
import dev.andrei.violaolivre.music.HarmonicField
import dev.andrei.violaolivre.music.Scale
import dev.andrei.violaolivre.music.ScaleType
import dev.andrei.violaolivre.music.pitchClassName
import dev.andrei.violaolivre.ui.components.ChoiceChips
import dev.andrei.violaolivre.ui.components.FretMark
import dev.andrei.violaolivre.ui.components.Fretboard
import dev.andrei.violaolivre.ui.components.MarkStyle
import dev.andrei.violaolivre.ui.components.PlayButton
import dev.andrei.violaolivre.ui.components.rememberNotePlayer

private const val LAST_FRET = 15

// Graus do campo maior que combinam bem com a pentatônica (sem o diminuto)
private val COMPATIBLE_DEGREES = listOf(0, 1, 2, 3, 4, 5)

@Composable
fun PentatonicLesson(onBack: () -> Unit) {
    val player = rememberNotePlayer()
    var root by rememberSaveable { mutableIntStateOf(9) } // Lá menor
    var box by rememberSaveable { mutableIntStateOf(0) }
    val scale = Scale(root, ScaleType.MINOR_PENTATONIC)
    val relativeMajor = (root + 3).mod(12) // o relativo maior fica 3 semitons acima
    val useFlats = HarmonicField.useFlats(relativeMajor)
    val rootName = pitchClassName(root, useFlats)
    val majorName = pitchClassName(relativeMajor, useFlats)

    val boxPositions = FretPositions.pentatonicBox(scale, box)
    // Todas as notas da escala no braço; as do desenho escolhido em destaque
    val marks = (0..5).flatMap { string -> (0..LAST_FRET).map { FretPosition(string, it) } }
        .filter { it.note().pitchClass in scale }
        .map { position ->
            val pitchClass = position.note().pitchClass
            val style = when {
                position !in boxPositions -> MarkStyle.FAINT
                pitchClass == root -> MarkStyle.ROOT
                else -> MarkStyle.NOTE
            }
            FretMark(position, pitchClassName(pitchClass, useFlats), style)
        }

    LessonScreen(stringResource(R.string.lesson_pentatonic_title), onBack) {
        LessonText(stringResource(R.string.pentatonic_intro))

        Text(stringResource(R.string.pentatonic_key), style = MaterialTheme.typography.labelLarge)
        ChoiceChips((0..11).toList(), root, { pitchClassName(it, HarmonicField.useFlats((it + 3).mod(12))) }, { root = it })
        Text(stringResource(R.string.pentatonic_box), style = MaterialTheme.typography.labelLarge)
        ChoiceChips((0..4).toList(), box, { (it + 1).toString() }, { box = it })

        Text(
            stringResource(
                R.string.pentatonic_notes,
                rootName,
                scale.pitchClasses.joinToString(" ") { pitchClassName(it, useFlats) },
            ),
            style = MaterialTheme.typography.titleMedium,
        )
        Fretboard(marks, lastFret = LAST_FRET, onPositionClick = { player.playNote(it.note()) })
        Text(
            stringResource(R.string.pentatonic_legend),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PlayButton(stringResource(R.string.pentatonic_play_box), onClick = {
            // Sobe pelo desenho (da 6ª corda para a 1ª) e desce de volta
            val up = boxPositions.map { it.note() }
            player.playSequence(up + up.reversed().drop(1), gapSeconds = 0.25)
        })
        LessonText(stringResource(R.string.pentatonic_relative, majorName))

        LessonHeading(stringResource(R.string.pentatonic_chords_heading))
        val chords = HarmonicField.major(relativeMajor)
            .filterIndexed { degree, _ -> degree in COMPATIBLE_DEGREES }
            .joinToString(", ") { it.name(useFlats) }
        LessonText(stringResource(R.string.pentatonic_chords_text, rootName, majorName, chords))
        LessonText(stringResource(R.string.pentatonic_tip))
    }
}
