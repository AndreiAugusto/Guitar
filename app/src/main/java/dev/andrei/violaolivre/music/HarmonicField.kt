package dev.andrei.violaolivre.music

/**
 * Campo harmônico maior: os 7 acordes formados empilhando terças com as notas
 * da própria escala maior. Por isso eles "combinam": usam só as notas do tom.
 */
object HarmonicField {

    /** Graus em algarismos romanos: maiúsculo = maior, minúsculo = menor, ° = diminuto. */
    val DEGREES = listOf("I", "ii", "iii", "IV", "V", "vi", "vii°")

    private val QUALITIES = listOf(
        ChordQuality.MAJOR, ChordQuality.MINOR, ChordQuality.MINOR, ChordQuality.MAJOR,
        ChordQuality.MAJOR, ChordQuality.MINOR, ChordQuality.DIMINISHED,
    )

    /** Tons em que todos os 7 acordes têm desenho na biblioteca (os mais usados no violão). */
    val GUITAR_KEYS = listOf(0, 2, 4, 5, 7, 9) // C D E F G A

    /** Os 7 acordes do campo harmônico de [root] maior. */
    fun major(root: Int): List<Chord> =
        Scale(root, ScaleType.MAJOR).pitchClasses.mapIndexed { degree, pitchClass ->
            Chord(pitchClass, QUALITIES[degree])
        }

    /** Tons escritos com bemol (Fá maior tem Si♭, não Lá♯). */
    fun useFlats(root: Int): Boolean = root.mod(12) in setOf(5, 10, 3, 8, 1)
}
