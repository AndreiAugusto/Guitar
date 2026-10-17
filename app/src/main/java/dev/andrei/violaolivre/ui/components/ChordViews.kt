package dev.andrei.violaolivre.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.music.Chord
import dev.andrei.violaolivre.music.ChordShape
import dev.andrei.violaolivre.music.FretPositions
import dev.andrei.violaolivre.music.pitchClassName

/** Nome do intervalo em português, a partir da distância em semitons até a fundamental. */
@Composable
fun intervalName(semitones: Int): String = stringResource(
    when (semitones) {
        0 -> R.string.interval_root
        3 -> R.string.interval_minor_third
        4 -> R.string.interval_major_third
        6 -> R.string.interval_diminished_fifth
        7 -> R.string.interval_fifth
        10 -> R.string.interval_minor_seventh
        11 -> R.string.interval_major_seventh
        else -> error("Intervalo sem nome: $semitones")
    },
)

/** Quadradinho da grade de acordes: nome + diagrama. */
@Composable
fun ChordTile(shape: ChordShape, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier,
    ) {
        Column(Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(shape.name, style = MaterialTheme.typography.titleMedium)
            ChordDiagram(shape, width = 80.dp)
        }
    }
}

/** As posições das notas do acorde no braço: a fundamental em azul, as outras em cinza. */
fun chordToneMarks(chord: Chord, useFlats: Boolean, lastFret: Int = 12): List<FretMark> =
    chord.pitchClasses.flatMap { pitchClass ->
        val style = if (pitchClass == chord.root) MarkStyle.ROOT else MarkStyle.NOTE
        FretPositions.of(pitchClass, lastFret).map { FretMark(it, pitchClassName(pitchClass, useFlats), style) }
    }

/**
 * Detalhe de um acorde: diagrama, as notas que o formam (com o papel de cada uma)
 * e onde essas notas estão em todo o braço. É aqui que se vê que "Lá menor" = Lá + Dó + Mi.
 */
@Composable
fun ChordDetail(
    chord: Chord,
    shape: ChordShape?,
    useFlats: Boolean,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val name = chord.name(useFlats)
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (shape != null) {
                ChordDiagram(shape, width = 120.dp)
                Spacer(Modifier.width(16.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(name, style = MaterialTheme.typography.headlineMedium)
                chord.quality.intervals.forEach { semitones ->
                    val pitchClass = (chord.root + semitones).mod(12)
                    Text(
                        "${pitchClassName(pitchClass, useFlats)} · ${intervalName(semitones)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (semitones == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (shape != null) {
                    OutlinedButton(onClick = onPlay) {
                        Icon(
                            painterResource(R.drawable.ic_volume_up),
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.chord_play))
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.chord_notes_on_fretboard, name), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        Fretboard(chordToneMarks(chord, useFlats))
    }
}
