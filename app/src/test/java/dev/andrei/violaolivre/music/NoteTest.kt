package dev.andrei.violaolivre.music

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteTest {

    @Test
    fun `A4 tem 440 Hz`() {
        assertEquals(440.0, Note(69).frequency, 1e-9)
    }

    @Test
    fun `cordas soltas da afinação padrão têm as frequências conhecidas`() {
        val expected = listOf(82.41, 110.00, 146.83, 196.00, 246.94, 329.63)
        Tunings.STANDARD.notes.zip(expected).forEach { (note, hz) ->
            assertEquals("frequência de $note", hz, note.frequency, 0.01)
        }
    }

    @Test
    fun `parse entende sustenido, bemol e oitava`() {
        assertEquals(Note(40), Note.parse("E2"))
        assertEquals(Note(54), Note.parse("F♯3"))
        assertEquals(Note(54), Note.parse("F#3"))
        assertEquals(Note(58), Note.parse("B♭3"))
        assertEquals(Note(58), Note.parse("Bb3"))
    }

    @Test
    fun `nomes com sustenido e com bemol`() {
        assertEquals("E2", Note(40).fullName())
        assertEquals("D♯2", Note(39).fullName())
        assertEquals("E♭2", Note(39).fullName(useFlats = true))
        assertEquals("C4", Note(60).fullName())
    }

    @Test
    fun `nearest arredonda para o semitom mais próximo`() {
        assertEquals(Note.parse("E2"), Note.nearest(82.41))
        assertEquals(Note.parse("E2"), Note.nearest(84.0))  // +33 cents: ainda E2
        assertEquals(Note.parse("F2"), Note.nearest(86.0))  // +74 cents: já é F2
        assertEquals(Note.parse("A4"), Note.nearest(436.0))
    }

    @Test
    fun `cents positivos acima, negativos abaixo, 100 por semitom`() {
        assertEquals(0.0, centsBetween(440.0, 440.0), 1e-9)
        assertEquals(1200.0, centsBetween(880.0, 440.0), 1e-9)
        assertEquals(100.0, centsBetween(Note(70).frequency, Note(69).frequency), 1e-9)
        assertEquals(-19.79, centsBetween(435.0, 440.0), 0.01)
    }
}
