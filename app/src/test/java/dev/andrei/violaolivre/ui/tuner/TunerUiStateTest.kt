package dev.andrei.violaolivre.ui.tuner

import dev.andrei.violaolivre.music.Note
import dev.andrei.violaolivre.music.Tunings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TunerUiStateTest {

    @Test
    fun `modo automático compara com a corda mais próxima`() {
        val state = TunerUiState(frequency = 108.0) // A2 baixa
        assertEquals(1, state.targetStringIndex)
        assertEquals(Note.parse("A2"), state.targetNote)
        assertTrue(state.cents!! < -IN_TUNE_CENTS)
        assertFalse(state.isInTune)
    }

    @Test
    fun `corda fixa compara com ela mesmo que o som esteja longe`() {
        // Afinando a 6ª corda do Drop D: o som ainda está em E2, bem acima de D2
        val state = TunerUiState(frequency = 82.41, tuning = Tunings.DROP_D, manualStringIndex = 0)
        assertEquals(Note.parse("D2"), state.targetNote)
        assertEquals(200.0, state.cents!!, 0.5)
    }

    @Test
    fun `corda fixa sem som já mostra a nota alvo`() {
        val state = TunerUiState(manualStringIndex = 3)
        assertEquals(Note.parse("G3"), state.targetNote)
        assertNull(state.cents)
    }

    @Test
    fun `modo cromático usa a nota mais próxima, não as cordas`() {
        val state = TunerUiState(frequency = 277.0, isChromatic = true) // C♯4
        assertNull(state.targetStringIndex)
        assertEquals(Note.parse("C♯4"), state.targetNote)
        assertFalse(state.useFlats)
    }

    @Test
    fun `afinada dentro de 5 cents`() {
        assertTrue(TunerUiState(frequency = 110.2).isInTune)
        assertFalse(TunerUiState(frequency = 112.0).isInTune)
    }

    @Test
    fun `sem som e sem corda fixa não há alvo`() {
        val state = TunerUiState()
        assertNull(state.targetNote)
        assertNull(state.cents)
    }
}
