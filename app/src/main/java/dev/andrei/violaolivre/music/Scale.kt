package dev.andrei.violaolivre.music

/** Tipos de escala, definidos pela distância (em semitons) de cada nota até a tônica. */
enum class ScaleType(val intervals: List<Int>) {
    MAJOR(listOf(0, 2, 4, 5, 7, 9, 11)),        // tom tom semitom tom tom tom semitom
    NATURAL_MINOR(listOf(0, 2, 3, 5, 7, 8, 10)),
    MAJOR_PENTATONIC(listOf(0, 2, 4, 7, 9)),    // a maior sem a 4ª e a 7ª
    MINOR_PENTATONIC(listOf(0, 3, 5, 7, 10)),   // a menor sem a 2ª e a 6ª
}

/** Uma escala concreta: tônica ([root], 0 = C) + tipo. */
data class Scale(val root: Int, val type: ScaleType) {
    /** As notas da escala (classes de altura), a partir da tônica. */
    val pitchClasses: List<Int> = type.intervals.map { (root + it).mod(12) }

    operator fun contains(pitchClass: Int): Boolean = pitchClass.mod(12) in pitchClasses
}
