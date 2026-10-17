package dev.andrei.violaolivre.audio

import dev.andrei.violaolivre.music.Accent
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Gera os cliques do metrônomo: um "tic" curto (senoide que some em poucos milissegundos).
 * O acento forte é mais agudo e mais alto, para se destacar dos outros tempos.
 */
object ClickSynth {

    private const val DURATION_SECONDS = 0.03
    private const val ATTACK_SECONDS = 0.001
    private const val DECAY_SECONDS = 0.008

    fun click(accent: Accent, sampleRate: Int): FloatArray {
        val (frequency, volume) = when (accent) {
            Accent.STRONG -> 1760.0 to 0.9
            Accent.MEDIUM -> 1320.0 to 0.7
            Accent.NONE -> 880.0 to 0.6
        }
        return FloatArray((sampleRate * DURATION_SECONDS).toInt()) { i ->
            val t = i.toDouble() / sampleRate
            val envelope = min(1.0, t / ATTACK_SECONDS) * exp(-t / DECAY_SECONDS)
            (volume * envelope * sin(2 * PI * frequency * t)).toFloat()
        }
    }

    /** Os três cliques prontos, um por tipo de acento. */
    fun allClicks(sampleRate: Int): Map<Accent, FloatArray> =
        Accent.entries.associateWith { click(it, sampleRate) }
}
