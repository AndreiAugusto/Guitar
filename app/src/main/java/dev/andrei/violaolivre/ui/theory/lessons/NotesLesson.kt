package dev.andrei.violaolivre.ui.theory.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.music.FretPosition
import dev.andrei.violaolivre.music.Note
import dev.andrei.violaolivre.music.isNatural
import dev.andrei.violaolivre.ui.components.FretMark
import dev.andrei.violaolivre.ui.components.Fretboard
import dev.andrei.violaolivre.ui.components.MarkStyle
import dev.andrei.violaolivre.ui.components.PlayButton
import dev.andrei.violaolivre.ui.components.rememberNotePlayer

// As 7 notas naturais na ordem das letras (de A a G), na oitava do meio do violão
private val NATURALS = listOf(
    "A3" to R.string.solfege_a, "B3" to R.string.solfege_b, "C4" to R.string.solfege_c,
    "D4" to R.string.solfege_d, "E4" to R.string.solfege_e, "F4" to R.string.solfege_f,
    "G4" to R.string.solfege_g,
)

private const val A_STRING = 1 // 5ª corda

@Composable
fun NotesLesson(onBack: () -> Unit) {
    val player = rememberNotePlayer()
    LessonScreen(stringResource(R.string.lesson_notes_title), onBack) {
        LessonText(stringResource(R.string.notes_intro))

        // As 7 letras: 4 numa linha, 3 na outra
        NATURALS.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (noteName, solfege) ->
                    val note = Note.parse(noteName)
                    Card(
                        onClick = { player.playNote(note) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(note.name(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                            Text(stringResource(solfege), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                // Completa a linha de 3 para os cartões terem a mesma largura
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Text(
            stringResource(R.string.notes_tap_to_hear),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LessonHeading(stringResource(R.string.notes_steps_heading))
        LessonText(stringResource(R.string.notes_steps_text))
        // A 5ª corda, casa por casa: as naturais em destaque, os ♯ apagados.
        // Repare que entre B e C, e entre E e F, não há casa no meio.
        Fretboard(
            marks = (0..12).map { fret ->
                val position = FretPosition(A_STRING, fret)
                val pitchClass = position.note().pitchClass
                FretMark(position, position.note().name(), if (isNatural(pitchClass)) MarkStyle.NOTE else MarkStyle.FAINT)
            },
            onPositionClick = { player.playNote(it.note()) },
        )

        LessonHeading(stringResource(R.string.notes_accidentals_heading))
        LessonText(stringResource(R.string.notes_accidentals_text))

        LessonHeading(stringResource(R.string.notes_octave_heading))
        LessonText(stringResource(R.string.notes_octave_text))
        PlayButton(stringResource(R.string.notes_octave_compare), onClick = {
            player.playSequence(listOf("A2", "A3", "A4").map(Note::parse), gapSeconds = 0.8)
        })

        LessonHeading(stringResource(R.string.notes_chords_heading))
        LessonText(stringResource(R.string.notes_chords_text))
    }
}
