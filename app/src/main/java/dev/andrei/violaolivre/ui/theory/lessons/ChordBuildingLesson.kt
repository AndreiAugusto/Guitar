package dev.andrei.violaolivre.ui.theory.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.music.Chord
import dev.andrei.violaolivre.music.ChordQuality
import dev.andrei.violaolivre.music.ChordShapes
import dev.andrei.violaolivre.music.pitchClassName
import dev.andrei.violaolivre.ui.components.ChoiceChips
import dev.andrei.violaolivre.ui.components.ChordDetail
import dev.andrei.violaolivre.ui.components.ChordTile
import dev.andrei.violaolivre.ui.components.rememberNotePlayer
import dev.andrei.violaolivre.ui.components.voicedNotes

@Composable
private fun qualityLabel(quality: ChordQuality): String = when (quality) {
    ChordQuality.MAJOR -> stringResource(R.string.quality_major)
    ChordQuality.MINOR -> stringResource(R.string.quality_minor)
    else -> quality.suffix
}

@Composable
fun ChordBuildingLesson(onBack: () -> Unit) {
    val player = rememberNotePlayer()
    var root by rememberSaveable { mutableIntStateOf(9) } // começa em Lá
    var quality by rememberSaveable { mutableStateOf(ChordQuality.MINOR) }
    val chord = Chord(root, quality)
    val shape = ChordShapes.forChord(chord)

    LessonScreen(stringResource(R.string.lesson_chord_building_title), onBack) {
        LessonText(stringResource(R.string.chord_building_intro))
        LessonText(stringResource(R.string.chord_building_triad))

        LessonHeading(stringResource(R.string.chord_building_third_heading))
        LessonText(stringResource(R.string.chord_building_third_text))
        // A e Am lado a lado: toque nos dois e compare
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(ChordQuality.MAJOR, ChordQuality.MINOR).forEach { q ->
                val compareShape = ChordShapes.forChord(Chord(9, q))!!
                ChordTile(compareShape, selected = false, onClick = { player.playChord(compareShape.notes()) }, modifier = Modifier.weight(1f))
            }
        }

        LessonHeading(stringResource(R.string.chord_building_seventh_heading))
        LessonText(stringResource(R.string.chord_building_seventh_text))

        LessonHeading(stringResource(R.string.chord_building_try_heading))
        Text(stringResource(R.string.chord_building_root), style = MaterialTheme.typography.labelLarge)
        ChoiceChips((0..11).toList(), root, { pitchClassName(it) }, { root = it })
        Text(stringResource(R.string.chord_building_quality), style = MaterialTheme.typography.labelLarge)
        ChoiceChips(ChordQuality.entries, quality, { qualityLabel(it) }, { quality = it })
        ChordDetail(
            chord = chord,
            shape = shape,
            useFlats = false,
            onPlay = { player.playChord(shape?.notes() ?: chord.voicedNotes()) },
        )
        if (shape == null) {
            Text(
                stringResource(R.string.chord_building_no_shape),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LessonText(stringResource(R.string.chord_building_repeat))
    }
}
