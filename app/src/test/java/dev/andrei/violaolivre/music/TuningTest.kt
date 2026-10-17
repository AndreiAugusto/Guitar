package dev.andrei.violaolivre.music

import org.junit.Assert.assertEquals
import org.junit.Test

class TuningTest {

    @Test
    fun `todas as afinações prontas batem com a tabela do README`() {
        val table = mapOf(
            "standard" to "E2 A2 D3 G3 B3 E4",
            "half_step_down" to "E♭2 A♭2 D♭3 G♭3 B♭3 E♭4",
            "whole_step_down" to "D2 G2 C3 F3 A3 D4",
            "drop_d" to "D2 A2 D3 G3 B3 E4",
            "drop_c" to "C2 G2 C3 F3 A3 D4",
            "dadgad" to "D2 A2 D3 G3 A3 D4",
            "open_g" to "D2 G2 D3 G3 B3 D4",
            "open_d" to "D2 A2 D3 F♯3 A3 D4",
            "open_e" to "E2 B2 E3 G♯3 B3 E4",
        )
        assertEquals(table.keys.toList(), Tunings.all.map { it.id })
        Tunings.all.forEach { tuning ->
            val names = tuning.notes.joinToString(" ") { it.fullName(tuning.useFlats) }
            assertEquals(tuning.id, table[tuning.id], names)
        }
    }

    @Test
    fun `corda mais próxima na afinação padrão`() {
        val standard = Tunings.STANDARD
        assertEquals(0, standard.closestStringIndex(80.0))   // E2 um pouco baixa
        assertEquals(1, standard.closestStringIndex(112.0))  // A2 um pouco alta
        assertEquals(5, standard.closestStringIndex(330.0))  // E4
        assertEquals(4, standard.closestStringIndex(240.0))  // entre G3 e B3, mais perto de B3
    }

    @Test
    fun `no Drop D a 6ª corda é D2`() {
        assertEquals(0, Tunings.DROP_D.closestStringIndex(73.0))
        assertEquals(Note.parse("D2"), Tunings.DROP_D.notes[0])
    }
}
