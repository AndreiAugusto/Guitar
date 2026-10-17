package dev.andrei.violaolivre.ui.theory

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.ui.components.ScreenHeader
import dev.andrei.violaolivre.ui.navigation.ChordBuildingLessonRoute
import dev.andrei.violaolivre.ui.navigation.ChordsRoute
import dev.andrei.violaolivre.ui.navigation.FretboardNotesRoute
import dev.andrei.violaolivre.ui.navigation.HarmonicFieldLessonRoute
import dev.andrei.violaolivre.ui.navigation.NotesLessonRoute
import dev.andrei.violaolivre.ui.navigation.PentatonicLessonRoute
import dev.andrei.violaolivre.ui.navigation.TabLessonRoute
import dev.andrei.violaolivre.ui.navigation.TuningsLessonRoute

/** Um tópico da lista: título, subtítulo e a rota da tela dele. */
enum class TheoryTopic(@StringRes val title: Int, @StringRes val subtitle: Int, val route: Any) {
    // Na ordem sugerida de estudo
    NOTES(R.string.lesson_notes_title, R.string.lesson_notes_subtitle, NotesLessonRoute),
    FRETBOARD_NOTES(R.string.theory_fretboard_title, R.string.theory_fretboard_subtitle, FretboardNotesRoute),
    TUNINGS(R.string.lesson_tunings_title, R.string.lesson_tunings_subtitle, TuningsLessonRoute),
    TAB(R.string.lesson_tab_title, R.string.lesson_tab_subtitle, TabLessonRoute),
    CHORD_BUILDING(R.string.lesson_chord_building_title, R.string.lesson_chord_building_subtitle, ChordBuildingLessonRoute),
    CHORDS(R.string.theory_chords_title, R.string.theory_chords_subtitle, ChordsRoute),
    HARMONIC_FIELD(R.string.lesson_harmonic_field_title, R.string.lesson_harmonic_field_subtitle, HarmonicFieldLessonRoute),
    PENTATONIC(R.string.lesson_pentatonic_title, R.string.lesson_pentatonic_subtitle, PentatonicLessonRoute),
}

@Composable
fun TheoryScreen(onOpenTopic: (route: Any) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        ScreenHeader(stringResource(R.string.tab_theory))
        // LazyColumn: lista que só desenha os itens visíveis (como um RecyclerView)
        LazyColumn(
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(TheoryTopic.entries) { topic ->
                Card(
                    onClick = { onOpenTopic(topic.route) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(topic.title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(topic.subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
