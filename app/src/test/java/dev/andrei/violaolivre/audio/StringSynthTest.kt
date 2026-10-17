package dev.andrei.violaolivre.audio

import dev.andrei.violaolivre.music.Tunings
import dev.andrei.violaolivre.music.centsBetween
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class StringSynthTest {

    private val detector = PitchDetector(MicListener.SAMPLE_RATE, MicListener.WINDOW_SIZE)

    @Test
    fun `nota de referência sai na frequência certa, do início ao fim`() {
        // Todas as notas de todas as afinações prontas
        val notes = Tunings.all.flatMap { it.notes }.toSet()
        notes.forEach { note ->
            val sound = StringSynth.pluck(note.frequency)
            // Confere logo depois do ataque e quando o som já está mais fraco
            listOf(0.1, 2.0).forEach { seconds ->
                val start = (seconds * MicListener.SAMPLE_RATE).toInt()
                val window = sound.copyOfRange(start, start + MicListener.WINDOW_SIZE)
                val result = detector.detect(window)
                assertNotNull("$note não detectada em ${seconds}s", result)
                val error = centsBetween(result!!.frequency, note.frequency)
                assertTrue("$note em ${seconds}s: erro de $error cents", abs(error) <= 1.0)
            }
        }
    }

    @Test
    fun `volume nunca passa do pico de 0,8`() {
        val sound = StringSynth.pluck(82.41)
        assertTrue(sound.maxOf { abs(it) } <= 0.8f + 1e-6f)
    }
}
