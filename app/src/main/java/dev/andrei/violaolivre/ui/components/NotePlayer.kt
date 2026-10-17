package dev.andrei.violaolivre.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.andrei.violaolivre.audio.ToneGenerator
import dev.andrei.violaolivre.music.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Toca notas e acordes a partir das telas (ex.: tocar numa casa do braço). */
class NotePlayer internal constructor(
    private val scope: CoroutineScope,
    private val generator: ToneGenerator,
) {
    private var job: Job? = null

    fun playNote(note: Note) = launch { generator.play(note.frequency) }

    fun playChord(notes: List<Note>) = launch { generator.playChord(notes.map { it.frequency }) }

    /** Notas uma depois da outra, como num riff ou arpejo. */
    fun playSequence(notes: List<Note>, gapSeconds: Double = 0.35) =
        launch { generator.playChord(notes.map { it.frequency }, gapSeconds) }

    // Um toque novo cancela o anterior, caso ele ainda esteja sendo sintetizado
    private fun launch(block: suspend () -> Unit) {
        job?.cancel()
        job = scope.launch { block() }
    }
}

/** Um [NotePlayer] que vive enquanto a tela estiver aberta e silencia ao sair dela. */
@Composable
fun rememberNotePlayer(): NotePlayer {
    val scope = rememberCoroutineScope()
    val generator = remember { ToneGenerator() }
    DisposableEffect(generator) {
        onDispose { generator.stop() }
    }
    return remember(scope, generator) { NotePlayer(scope, generator) }
}
