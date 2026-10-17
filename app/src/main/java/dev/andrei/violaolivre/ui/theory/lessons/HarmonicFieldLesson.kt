package dev.andrei.violaolivre.ui.theory.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.music.ChordShapes
import dev.andrei.violaolivre.music.HarmonicField
import dev.andrei.violaolivre.music.Scale
import dev.andrei.violaolivre.music.ScaleType
import dev.andrei.violaolivre.music.pitchClassName
import dev.andrei.violaolivre.ui.components.ChoiceChips
import dev.andrei.violaolivre.ui.components.ChordTile
import dev.andrei.violaolivre.ui.components.rememberNotePlayer

@Composable
fun HarmonicFieldLesson(onBack: () -> Unit) {
    val player = rememberNotePlayer()
    var key by rememberSaveable { mutableIntStateOf(0) } // Dó
    val useFlats = HarmonicField.useFlats(key)
    val keyName = pitchClassName(key, useFlats)
    val scaleNotes = Scale(key, ScaleType.MAJOR).pitchClasses.joinToString(" ") { pitchClassName(it, useFlats) }

    LessonScreen(stringResource(R.string.lesson_harmonic_field_title), onBack) {
        LessonText(stringResource(R.string.field_intro))
        LessonText(stringResource(R.string.field_recipe))

        Text(stringResource(R.string.field_key), style = MaterialTheme.typography.labelLarge)
        ChoiceChips(HarmonicField.GUITAR_KEYS, key, { pitchClassName(it, HarmonicField.useFlats(it)) }, { key = it })
        Text(stringResource(R.string.field_scale, keyName, scaleNotes), style = MaterialTheme.typography.titleMedium)

        // Os 7 acordes em linhas de 3, cada um com o grau (I, ii...) em cima
        HarmonicField.major(key).zip(HarmonicField.DEGREES).chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (chord, degree) ->
                    // GUITAR_KEYS só tem tons em que todos os acordes têm desenho
                    val shape = ChordShapes.forChord(chord)!!.copy(useFlats = useFlats)
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(degree, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        ChordTile(shape, selected = false, onClick = { player.playChord(shape.notes()) })
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }

        LessonHeading(stringResource(R.string.field_why_heading))
        LessonText(stringResource(R.string.field_why_text))
        LessonHeading(stringResource(R.string.field_common_heading))
        LessonText(stringResource(R.string.field_common_text))
    }
}
