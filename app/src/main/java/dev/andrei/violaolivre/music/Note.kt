package dev.andrei.violaolivre.music

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Uma nota musical, identificada pelo número MIDI: cada semitom soma 1.
 * Exemplos: E2 (6ª corda) = 40, A4 (o "lá" de 440 Hz) = 69.
 */
data class Note(val midi: Int) {

    /** A nota sem a oitava, de 0 (C) a 11 (B): todo Lá é 9, em qualquer oitava. */
    val pitchClass: Int get() = midi.mod(12)

    /** Oitava no padrão científico: o dó central é C4. */
    val octave: Int get() = midi.floorDiv(12) - 1

    /** Frequência em Hz na afinação temperada (cada semitom multiplica por 2^(1/12)). */
    val frequency: Double get() = A4_FREQUENCY * 2.0.pow((midi - A4_MIDI) / 12.0)

    /** Nome sem a oitava, ex.: "F♯". Com [useFlats], usa bemóis: "G♭". */
    fun name(useFlats: Boolean = false): String = pitchClassName(pitchClass, useFlats)

    /** Nome com a oitava, ex.: "E2". */
    fun fullName(useFlats: Boolean = false): String = name(useFlats) + octave

    override fun toString(): String = fullName()

    companion object {
        const val A4_FREQUENCY = 440.0
        const val A4_MIDI = 69

        internal val SHARP_NAMES = listOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")
        internal val FLAT_NAMES = listOf("C", "D♭", "D", "E♭", "E", "F", "G♭", "G", "A♭", "A", "B♭", "B")
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

/** Nome de uma classe de altura (0 = C ... 11 = B), com sustenido ou bemol. */
fun pitchClassName(pitchClass: Int, useFlats: Boolean = false): String {
    val names = if (useFlats) Note.FLAT_NAMES else Note.SHARP_NAMES
    return names[pitchClass.mod(12)]
}

/** true para as 7 notas naturais (sem ♯/♭): C D E F G A B. */
fun isNatural(pitchClass: Int): Boolean = pitchClass.mod(12) in setOf(0, 2, 4, 5, 7, 9, 11)

/**
 * Distância em cents de [frequency] até [reference]. 100 cents = 1 semitom.
 * Positivo: a frequência está acima (corda "alta"); negativo: abaixo ("baixa").
 */
fun centsBetween(frequency: Double, reference: Double): Double = 1200 * log2(frequency / reference)
