package dev.andrei.violaolivre.ui.theory.lessons

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.music.Chord
import dev.andrei.violaolivre.music.ChordQuality
import dev.andrei.violaolivre.music.ChordShapes
import dev.andrei.violaolivre.music.FretPosition
import dev.andrei.violaolivre.ui.components.PlayButton
import dev.andrei.violaolivre.ui.components.rememberNotePlayer

// Arpejo de Lá menor (o mesmo do mockup). Cada coluna de números é uma nota, da esquerda para a direita.
private val RIFF_TAB = """
    e|-----0---0---|
    B|---1---1---1-|
    G|-2-----------|
    D|-------------|
    A|-------------|
    E|-------------|
""".trimIndent()

// As mesmas notas do RIFF_TAB, como posições (corda 3 = G, 4 = B, 5 = e)
private val RIFF_NOTES = listOf(
    FretPosition(3, 2), FretPosition(4, 1), FretPosition(5, 0),
    FretPosition(4, 1), FretPosition(5, 0), FretPosition(4, 1),
)

private val C_CHORD_TAB = """
    e|-0-|
    B|-1-|
    G|-0-|
    D|-2-|
    A|-3-|
    E|-x-|
""".trimIndent()

@Composable
fun TabLesson(onBack: () -> Unit) {
    val player = rememberNotePlayer()
    LessonScreen(stringResource(R.string.lesson_tab_title), onBack) {
        LessonText(stringResource(R.string.tab_intro))
        TabBlock(RIFF_TAB)
        LessonText(stringResource(R.string.tab_reading_text))
        PlayButton(stringResource(R.string.tab_play_riff), onClick = {
            player.playSequence(RIFF_NOTES.map { it.note() })
        })

        LessonHeading(stringResource(R.string.tab_chord_heading))
        LessonText(stringResource(R.string.tab_chord_text))
        TabBlock(C_CHORD_TAB)
        PlayButton(stringResource(R.string.tab_play_chord), onClick = {
            ChordShapes.forChord(Chord(0, ChordQuality.MAJOR))?.let { player.playChord(it.notes()) }
        })

        LessonHeading(stringResource(R.string.tab_symbols_heading))
        listOf(
            R.string.tab_symbol_hammer, R.string.tab_symbol_pull, R.string.tab_symbol_slide,
            R.string.tab_symbol_bend, R.string.tab_symbol_vibrato, R.string.tab_symbol_mute,
            R.string.tab_symbol_bar,
        ).forEach { LessonText(stringResource(it)) }
        LessonText(stringResource(R.string.tab_rhythm_text))
    }
}
