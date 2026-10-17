package dev.andrei.violaolivre.ui.theory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.music.FretPosition
import dev.andrei.violaolivre.music.isNatural
import dev.andrei.violaolivre.music.pitchClassName
import dev.andrei.violaolivre.ui.components.FretMark
import dev.andrei.violaolivre.ui.components.Fretboard
import dev.andrei.violaolivre.ui.components.MarkStyle
import dev.andrei.violaolivre.ui.components.ScreenHeader
import dev.andrei.violaolivre.ui.components.rememberNotePlayer

private const val LAST_FRET = 12

/** Mapa das notas no braço: destaque de uma nota, ♯ opcionais e som ao tocar numa casa. */
@Composable
fun FretboardNotesScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    // rememberSaveable: o estado sobrevive a girar a tela (remember sozinho seria perdido)
    var showAccidentals by rememberSaveable { mutableStateOf(false) }
    var highlighted by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedString by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedFret by rememberSaveable { mutableStateOf<Int?>(null) }
    val selected = if (selectedString != null && selectedFret != null) FretPosition(selectedString!!, selectedFret!!) else null
    val player = rememberNotePlayer()

    val marks = (0..5).flatMap { string ->
        (0..LAST_FRET).mapNotNull { fret ->
            val position = FretPosition(string, fret)
            val pitchClass = position.note().pitchClass
            val isHighlighted = pitchClass == highlighted
            if (!showAccidentals && !isNatural(pitchClass) && !isHighlighted) return@mapNotNull null
            val style = when {
                isHighlighted -> MarkStyle.ROOT
                highlighted != null -> MarkStyle.FAINT
                else -> MarkStyle.NOTE
            }
            FretMark(position, pitchClassName(pitchClass), style)
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        ScreenHeader(stringResource(R.string.theory_fretboard_title), onBack = onBack)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.fretboard_intro), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        Text(stringResource(R.string.fretboard_highlight), style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
            items(12) { pitchClass ->
                FilterChip(
                    selected = highlighted == pitchClass,
                    onClick = { highlighted = if (highlighted == pitchClass) null else pitchClass },
                    label = { Text(pitchClassName(pitchClass)) },
                )
            }
        }
        FilterChip(
            selected = showAccidentals,
            onClick = { showAccidentals = !showAccidentals },
            label = { Text(stringResource(R.string.fretboard_show_accidentals)) },
        )

        Spacer(Modifier.height(12.dp))
        Fretboard(
            marks = marks,
            lastFret = LAST_FRET,
            selected = selected,
            onPositionClick = { position ->
                selectedString = position.string
                selectedFret = position.fret
                player.playNote(position.note())
            },
        )
        Spacer(Modifier.height(8.dp))

        val stringNumber = selected?.let { 6 - it.string }
        Text(
            text = when {
                selected == null -> stringResource(R.string.fretboard_tip)
                selected.fret == 0 -> stringResource(R.string.fretboard_position_open, stringNumber!!, selected.note().fullName())
                else -> stringResource(R.string.fretboard_position, stringNumber!!, selected.fret, selected.note().fullName())
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (selected == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        )
    }
}
