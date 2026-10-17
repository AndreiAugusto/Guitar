package dev.andrei.violaolivre.music

/** Intensidade de um tempo do compasso. */
enum class Accent { STRONG, MEDIUM, NONE }

/**
 * Fórmula de compasso: quantos cliques por compasso e qual o acento de cada um.
 * No 6/8 cada clique é uma colcheia; o 4º tempo tem acento leve ("UM-dois-três-QUA-tro-cin-co").
 */
enum class TimeSignature(val label: String, private val pattern: List<Accent>) {
    TWO_FOUR("2/4", listOf(Accent.STRONG, Accent.NONE)),
    THREE_FOUR("3/4", listOf(Accent.STRONG, Accent.NONE, Accent.NONE)),
    FOUR_FOUR("4/4", listOf(Accent.STRONG, Accent.NONE, Accent.NONE, Accent.NONE)),
    SIX_EIGHT("6/8", listOf(Accent.STRONG, Accent.NONE, Accent.NONE, Accent.MEDIUM, Accent.NONE, Accent.NONE));

    val beatsPerBar: Int get() = pattern.size

    /** Acento do tempo [beat] (0 = primeiro). Sem [accentEnabled], todos soam iguais. */
    fun accentOf(beat: Int, accentEnabled: Boolean = true): Accent =
        if (accentEnabled) pattern[beat] else Accent.NONE
}
