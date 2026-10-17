package dev.andrei.violaolivre.ui.theory.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.music.Tuning
import dev.andrei.violaolivre.music.Tunings
import dev.andrei.violaolivre.ui.components.PlayButton
import dev.andrei.violaolivre.ui.components.rememberNotePlayer
import dev.andrei.violaolivre.ui.components.tuningName

private fun descriptionOf(tuning: Tuning): Int = when (tuning.id) {
    "standard" -> R.string.tuning_desc_standard
    "half_step_down" -> R.string.tuning_desc_half_step_down
    "whole_step_down" -> R.string.tuning_desc_whole_step_down
    "drop_d" -> R.string.tuning_desc_drop_d
    "drop_c" -> R.string.tuning_desc_drop_c
    "dadgad" -> R.string.tuning_desc_dadgad
    "open_g" -> R.string.tuning_desc_open_g
    "open_d" -> R.string.tuning_desc_open_d
    "open_e" -> R.string.tuning_desc_open_e
    else -> error("Afinação sem descrição: ${tuning.id}")
}

@Composable
fun TuningsLesson(onBack: () -> Unit) {
    val player = rememberNotePlayer()
    LessonScreen(stringResource(R.string.lesson_tunings_title), onBack) {
        LessonText(stringResource(R.string.tunings_intro))
        Tunings.all.forEach { tuning ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tuningName(tuning), style = MaterialTheme.typography.titleMedium)
                    // As 6 cordas, da 6ª à 1ª; as que diferem da padrão em destaque
                    Row(Modifier.fillMaxWidth()) {
                        tuning.notes.forEachIndexed { string, note ->
                            val changed = note != Tunings.STANDARD.notes[string]
                            Text(
                                note.name(tuning.useFlats),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = if (changed) FontWeight.Bold else FontWeight.Normal,
                                color = if (changed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Text(stringResource(descriptionOf(tuning)), style = MaterialTheme.typography.bodyMedium)
                    PlayButton(stringResource(R.string.tunings_play), onClick = {
                        player.playSequence(tuning.notes, gapSeconds = 0.3)
                    })
                }
            }
        }
        LessonText(stringResource(R.string.tunings_tip))
    }
}
