package dev.andrei.violaolivre.music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TheoryTest {

    private fun names(pitchClasses: Collection<Int>, useFlats: Boolean = false) =
        pitchClasses.joinToString(" ") { pitchClassName(it, useFlats) }

    @Test
    fun `escalas maior, menor e pentatônicas`() {
        assertEquals("C D E F G A B", names(Scale(0, ScaleType.MAJOR).pitchClasses))
        assertEquals("A B C D E F G", names(Scale(9, ScaleType.NATURAL_MINOR).pitchClasses))
        assertEquals("A C D E G", names(Scale(9, ScaleType.MINOR_PENTATONIC).pitchClasses))
        assertEquals("G A B D E", names(Scale(7, ScaleType.MAJOR_PENTATONIC).pitchClasses))
    }

    @Test
    fun `nomes e notas dos acordes`() {
        assertEquals("Am", Chord(9, ChordQuality.MINOR).name())
        assertEquals("F♯m", Chord(6, ChordQuality.MINOR).name())
        assertEquals("B♭", Chord(10, ChordQuality.MAJOR).name(useFlats = true))
        assertEquals("C7M", Chord(0, ChordQuality.MAJOR_7).name())
        assertEquals(setOf(7, 11, 2, 5), Chord(7, ChordQuality.DOMINANT_7).pitchClasses) // G B D F
    }

    @Test
    fun `campo harmônico de Dó, Sol e Fá`() {
        fun field(root: Int) = HarmonicField.major(root).joinToString(" ") { it.name(HarmonicField.useFlats(root)) }
        assertEquals("C Dm Em F G Am B°", field(0))
        assertEquals("G Am Bm C D Em F♯°", field(7))
        assertEquals("F Gm Am B♭ C Dm E°", field(5))
    }

    @Test
    fun `Am, C e G estão juntos nos campos de Dó e de Sol`() {
        val trio = setOf(Chord(9, ChordQuality.MINOR), Chord(0, ChordQuality.MAJOR), Chord(7, ChordQuality.MAJOR))
        assertTrue(HarmonicField.major(0).containsAll(trio))
        assertTrue(HarmonicField.major(7).containsAll(trio))
    }

    @Test
    fun `cada desenho de acorde soa só notas do acorde, com a fundamental no baixo`() {
        ChordShapes.all.forEach { shape ->
            val notes = shape.notes()
            val sounding = notes.map { it.pitchClass }.toSet()
            val chordNotes = shape.chord.pitchClasses
            // Em acordes de 4 notas a 5ª pode ser omitida (ex.: o C7 aberto, x32310)
            val fifth = (shape.chord.root + 7).mod(12)
            val required = if (chordNotes.size == 4) chordNotes - fifth else chordNotes
            assertTrue("${shape.name} tem nota fora do acorde: $sounding", chordNotes.containsAll(sounding))
            assertTrue("${shape.name} não tem todas as notas: $sounding", sounding.containsAll(required))
            assertEquals("baixo de ${shape.name}", shape.chord.root, notes.first().pitchClass)
            // Cordas soltas ou abafadas não usam dedo; cordas presas usam
            shape.frets.zip(shape.fingers).forEach { (fret, finger) ->
                assertEquals("dedo em ${shape.name}", fret > 0, finger > 0)
            }
        }
    }

    @Test
    fun `nenhum acorde repetido na biblioteca`() {
        val chords = ChordShapes.all.map { it.chord }
        assertEquals(chords.size, chords.toSet().size)
    }

    @Test
    fun `todos os acordes dos tons do violão têm desenho`() {
        HarmonicField.GUITAR_KEYS.forEach { key ->
            HarmonicField.major(key).forEach { chord ->
                assertNotNull("falta desenho de ${chord.name()} (tom $key)", ChordShapes.forChord(chord))
            }
        }
    }

    @Test
    fun `casa inicial do diagrama`() {
        assertEquals(1, ChordShapes.forChord(Chord(0, ChordQuality.MAJOR))!!.baseFret)  // C aberto
        assertEquals(1, ChordShapes.forChord(Chord(11, ChordQuality.MINOR))!!.baseFret) // Bm na casa 2 cabe a partir da 1
        assertEquals(4, ChordShapes.forChord(Chord(8, ChordQuality.MINOR))!!.baseFret)  // G♯m na casa 4
    }

    @Test
    fun `posições de uma nota no braço`() {
        // Lá (A) de 0 a 12: 6ª casa 5, 5ª solta e 12, 4ª casa 7, 3ª casa 2, 2ª casa 10, 1ª casa 5
        val expected = setOf(
            FretPosition(0, 5), FretPosition(1, 0), FretPosition(1, 12), FretPosition(2, 7),
            FretPosition(3, 2), FretPosition(4, 10), FretPosition(5, 5),
        )
        assertEquals(expected, FretPositions.of(9).toSet())
    }

    @Test
    fun `desenhos 1 e 2 da pentatônica de Lá menor`() {
        val aMinor = Scale(9, ScaleType.MINOR_PENTATONIC)
        fun frets(box: Int) = FretPositions.pentatonicBox(aMinor, box).groupBy({ it.string }, { it.fret })
            .values.joinToString(" ") { it.joinToString("-") }
        assertEquals("5-8 5-7 5-7 5-7 5-8 5-8", frets(0))
        assertEquals("8-10 7-10 7-10 7-9 8-10 8-10", frets(1))
    }

    @Test
    fun `os 5 desenhos cobrem só notas da escala e nunca casas negativas`() {
        (0..11).forEach { root ->
            val scale = Scale(root, ScaleType.MINOR_PENTATONIC)
            (0..4).forEach { box ->
                val positions = FretPositions.pentatonicBox(scale, box)
                assertEquals(12, positions.size)
                assertTrue(positions.all { it.fret >= 0 && it.note().pitchClass in scale })
                val span = positions.maxOf { it.fret } - positions.minOf { it.fret }
                assertTrue("desenho $box do tom $root abre $span casas", span <= 4)
            }
        }
    }
}
