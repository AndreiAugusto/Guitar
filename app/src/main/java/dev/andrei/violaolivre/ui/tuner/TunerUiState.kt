package dev.andrei.violaolivre.ui.tuner

import dev.andrei.violaolivre.music.Note
import dev.andrei.violaolivre.music.Tuning
import dev.andrei.violaolivre.music.Tunings
import dev.andrei.violaolivre.music.centsBetween
import kotlin.math.abs

/** Até quantos cents de diferença a corda é considerada afinada. */
const val IN_TUNE_CENTS = 5.0

/** Tudo o que a tela do afinador precisa para se desenhar. */
data class TunerUiState(
    /** Frequência suavizada em Hz; null quando nenhuma corda está soando. */
    val frequency: Double? = null,
    /** true quando a corda parou de soar e a tela só está mantendo a última leitura. */
    val isHolding: Boolean = false,
    val micError: Boolean = false,
    val tuning: Tuning = Tunings.STANDARD,
    /** Modo cromático: compara com qualquer nota, ignorando as cordas da afinação. */
    val isChromatic: Boolean = false,
    /** Corda escolhida tocando nela (0 = 6ª); null = detecção automática da corda. */
    val manualStringIndex: Int? = null,
) {
    /** Corda que está sendo afinada agora, ou null (modo cromático, ou automático sem som). */
    val targetStringIndex: Int?
        get() = when {
            isChromatic -> null
            manualStringIndex != null -> manualStringIndex
            frequency != null -> tuning.closestStringIndex(frequency)
            else -> null
        }

    /** Nota de referência: a da corda alvo ou, no modo cromático, a nota mais próxima do som. */
    val targetNote: Note?
        get() = if (isChromatic) {
            frequency?.let(Note::nearest)
        } else {
            targetStringIndex?.let { tuning.notes[it] }
        }

    /** Desvio em cents até a nota de referência (positivo = alta, negativo = baixa). */
    val cents: Double?
        get() {
            val hz = frequency ?: return null
            val note = targetNote ?: return null
            return centsBetween(hz, note.frequency)
        }

    val isInTune: Boolean get() = cents?.let { abs(it) <= IN_TUNE_CENTS } == true

    /** Nomes com bemol só nas afinações escritas com bemol (ex.: meio tom abaixo). */
    val useFlats: Boolean get() = !isChromatic && tuning.useFlats
}
