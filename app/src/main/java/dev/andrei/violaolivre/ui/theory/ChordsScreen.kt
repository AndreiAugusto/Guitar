package dev.andrei.violaolivre.ui.theory

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
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
import dev.andrei.violaolivre.music.ChordQuality
import dev.andrei.violaolivre.music.ChordShapes
import dev.andrei.violaolivre.ui.components.ChordDetail
import dev.andrei.violaolivre.ui.components.ChordTile
import dev.andrei.violaolivre.ui.components.ScreenHeader
import dev.andrei.violaolivre.ui.components.rememberNotePlayer

/** Filtros da grade de acordes. */
private enum class ChordFilter(@StringRes val label: Int, val qualities: Set<ChordQuality>) {
    ALL(R.string.chords_filter_all, ChordQuality.entries.toSet()),
    MAJOR(R.string.chords_filter_major, setOf(ChordQuality.MAJOR)),
    MINOR(R.string.chords_filter_minor, setOf(ChordQuality.MINOR)),
    SEVENTH(R.string.chords_filter_seventh, setOf(ChordQuality.DOMINANT_7, ChordQuality.MAJOR_7, ChordQuality.MINOR_7)),
    DIMINISHED(R.string.chords_filter_diminished, setOf(ChordQuality.DIMINISHED)),
}

/** Biblioteca de acordes: grade de diagramas; tocar num acorde mostra as notas dele e toca o som. */
@Composable
fun ChordsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var filter by rememberSaveable { mutableStateOf(ChordFilter.ALL) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) } // índice em ChordShapes.all
    val selected = ChordShapes.all[selectedIndex]
    val player = rememberNotePlayer()
    val shapes = ChordShapes.all.filter { it.chord.quality in filter.qualities }

    // Uma grade só para a tela toda: os itens de "cabeçalho" ocupam a linha inteira
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 104.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                ScreenHeader(stringResource(R.string.theory_chords_title), onBack = onBack)
                Text(stringResource(R.string.chords_intro), style = MaterialTheme.typography.bodyMedium)
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            ChordDetail(
                chord = selected.chord,
                shape = selected,
                useFlats = selected.useFlats,
                onPlay = { player.playChord(selected.notes()) },
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ChordFilter.entries) { option ->
                    FilterChip(
                        selected = option == filter,
                        onClick = { filter = option },
                        label = { Text(stringResource(option.label)) },
                    )
                }
            }
        }
        items(shapes, key = { it.name }) { shape ->
            ChordTile(
                shape = shape,
                selected = shape == selected,
                onClick = {
                    selectedIndex = ChordShapes.all.indexOf(shape)
                    player.playChord(shape.notes())
                },
            )
        }
    }
}
