package dev.andrei.violaolivre.music

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Uma nota musical, identificada pelo número MIDI: cada semitom soma 1.
 * Exemplos: E2 (6ª corda) = 40, A4 (o "lá" de 440 Hz) = 69.
 */
data class Note(val midi: Int) {

    /** Oitava no padrão científico: o dó central é C4. */
    val octave: Int get() = midi.floorDiv(12) - 1

    /** Frequência em Hz na afinação temperada (cada semitom multiplica por 2^(1/12)). */
    val frequency: Double get() = A4_FREQUENCY * 2.0.pow((midi - A4_MIDI) / 12.0)

    /** Nome sem a oitava, ex.: "F♯". Com [useFlats], usa bemóis: "G♭". */
    fun name(useFlats: Boolean = false): String {
        val names = if (useFlats) FLAT_NAMES else SHARP_NAMES
        return names[midi.mod(12)]
    }

    /** Nome com a oitava, ex.: "E2". */
    fun fullName(useFlats: Boolean = false): String = name(useFlats) + octave

    override fun toString(): String = fullName()

    companion object {
        const val A4_FREQUENCY = 440.0
        const val A4_MIDI = 69

        private val SHARP_NAMES = listOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")
        private val FLAT_NAMES = listOf("C", "D♭", "D", "E♭", "E", "F", "G♭", "G", "A♭", "A", "B♭", "B")
        private val LETTER_OFFSETS = mapOf('C' to 0, 'D' to 2, 'E' to 4, 'F' to 5, 'G' to 7, 'A' to 9, 'B' to 11)

        /** A nota da escala temperada mais próxima de uma frequência. */
        fun nearest(frequency: Double): Note {
            val semitonesFromA4 = 12 * log2(frequency / A4_FREQUENCY)
            return Note(A4_MIDI + semitonesFromA4.roundToInt())
        }

        /** Lê nomes como "E2", "F♯3", "Bb3" (aceita ♯/# e ♭/b). */
        fun parse(text: String): Note {
            val letter = text.first().uppercaseChar()
            val offset = LETTER_OFFSETS[letter] ?: throw IllegalArgumentException("Nota inválida: $text")
            var rest = text.substring(1)
            val accidental = when (rest.firstOrNull()) {
                '♯', '#' -> 1
                '♭', 'b' -> -1
                else -> 0
            }
            if (accidental != 0) rest = rest.substring(1)
            val octave = rest.toIntOrNull() ?: throw IllegalArgumentException("Oitava inválida: $text")
            return Note((octave + 1) * 12 + offset + accidental)
        }
    }
}

/**
 * Distância em cents de [frequency] até [reference]. 100 cents = 1 semitom.
 * Positivo: a frequência está acima (corda "alta"); negativo: abaixo ("baixa").
 */
fun centsBetween(frequency: Double, reference: Double): Double = 1200 * log2(frequency / reference)
