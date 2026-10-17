package dev.andrei.violaolivre.music

/**
 * Tipo de acorde: o sufixo do nome (no padrão brasileiro) e as notas em semitons
 * a partir da fundamental. Ex.: maior = fundamental (0) + 3ª maior (4) + 5ª justa (7).
 */
enum class ChordQuality(val suffix: String, val intervals: List<Int>) {
    MAJOR("", listOf(0, 4, 7)),
    MINOR("m", listOf(0, 3, 7)),
    DIMINISHED("°", listOf(0, 3, 6)),
    DOMINANT_7("7", listOf(0, 4, 7, 10)),
    MAJOR_7("7M", listOf(0, 4, 7, 11)),
    MINOR_7("m7", listOf(0, 3, 7, 10)),
}

/** Um acorde: fundamental ([root], 0 = C) + tipo. */
data class Chord(val root: Int, val quality: ChordQuality) {
    val pitchClasses: Set<Int> get() = quality.intervals.map { (root + it).mod(12) }.toSet()

    /** Nome como "Am", "G7", "F♯m", "B°". */
    fun name(useFlats: Boolean = false): String = pitchClassName(root, useFlats) + quality.suffix
}
