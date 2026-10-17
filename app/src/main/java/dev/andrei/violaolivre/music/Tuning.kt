package dev.andrei.violaolivre.music

import kotlin.math.abs

/**
 * Uma afinação: as notas das 6 cordas, da 6ª (mais grave) para a 1ª (mais aguda).
 * [id] identifica a afinação (o nome exibido vem de strings.xml).
 * [useFlats] escolhe se os nomes aparecem com bemol (E♭) ou sustenido (D♯).
 */
data class Tuning(
    val id: String,
    val notes: List<Note>,
    val useFlats: Boolean = false,
) {
    init {
        require(notes.size == 6) { "Uma afinação de violão tem 6 cordas" }
    }

    /** Índice (0 = 6ª corda) da corda cuja nota está mais perto da frequência, em cents. */
    fun closestStringIndex(frequency: Double): Int =
        notes.indices.minBy { abs(centsBetween(frequency, notes[it].frequency)) }
}

/** As afinações prontas da tabela do README. */
object Tunings {
    private fun tuning(id: String, notes: String, useFlats: Boolean = false) =
        Tuning(id, notes.split(" ").map(Note::parse), useFlats)

    val STANDARD = tuning("standard", "E2 A2 D3 G3 B3 E4")
    val HALF_STEP_DOWN = tuning("half_step_down", "E♭2 A♭2 D♭3 G♭3 B♭3 E♭4", useFlats = true)
    val WHOLE_STEP_DOWN = tuning("whole_step_down", "D2 G2 C3 F3 A3 D4")
    val DROP_D = tuning("drop_d", "D2 A2 D3 G3 B3 E4")
    val DROP_C = tuning("drop_c", "C2 G2 C3 F3 A3 D4")
    val DADGAD = tuning("dadgad", "D2 A2 D3 G3 A3 D4")
    val OPEN_G = tuning("open_g", "D2 G2 D3 G3 B3 D4")
    val OPEN_D = tuning("open_d", "D2 A2 D3 F♯3 A3 D4")
    val OPEN_E = tuning("open_e", "E2 B2 E3 G♯3 B3 E4")

    val all = listOf(
        STANDARD, HALF_STEP_DOWN, WHOLE_STEP_DOWN, DROP_D, DROP_C,
        DADGAD, OPEN_G, OPEN_D, OPEN_E,
    )
}
