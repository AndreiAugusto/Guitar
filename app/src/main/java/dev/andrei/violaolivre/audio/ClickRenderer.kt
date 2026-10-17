package dev.andrei.violaolivre.audio

import dev.andrei.violaolivre.music.Accent
import dev.andrei.violaolivre.music.TimeSignature
import kotlin.math.max

/**
 * Coração do metrônomo: preenche pedaços de áudio com silêncio e cliques
 * colocados na amostra exata de cada tempo.
 *
 * O relógio é a contagem de amostras: a 120 BPM e 44 100 amostras/s, um tempo
 * a cada 22 050 amostras. Como a posição do próximo tempo é calculada (e não
 * esperada com um timer), o erro não se acumula, mesmo depois de horas.
 *
 * Kotlin puro, para poder ser testado no computador.
 */
class ClickRenderer(
    private val sampleRate: Int,
    private val clicks: Map<Accent, FloatArray> = ClickSynth.allClicks(sampleRate),
) {
    /** Um tempo que começou a soar: [samplePosition] é a amostra do início, contada desde o play. */
    data class Beat(val samplePosition: Long, val indexInBar: Int, val accent: Accent)

    var bpm: Int = 120
        set(value) {
            field = value
            // Reagenda o próximo tempo a partir do último, para a mudança ser sentida já
            // (sem isso, ir de 40 para 200 BPM esperaria o 1,5 s do intervalo antigo)
            if (lastBeatAt >= 0) nextBeatAt = max(lastBeatAt + samplesPerBeat, position.toDouble())
        }

    var timeSignature: TimeSignature = TimeSignature.FOUR_FOUR
        set(value) {
            field = value
            nextIndexInBar = 0 // o próximo tempo vira o "1" do compasso novo
        }

    var accentEnabled: Boolean = true

    private val samplesPerBeat: Double get() = sampleRate * 60.0 / bpm

    private var position = 0L          // amostras já entregues desde o início
    private var nextBeatAt = 0.0       // posição (fracionária) do próximo tempo
    private var lastBeatAt = -1.0
    private var nextIndexInBar = 0

    // Um clique dura ~30 ms e pode começar no fim de um pedaço e terminar no seguinte
    private var activeClick: FloatArray? = null
    private var activeOffset = 0

    /** Volta ao início: o próximo render começa com o tempo 1. */
    fun reset() {
        position = 0
        nextBeatAt = 0.0
        lastBeatAt = -1.0
        nextIndexInBar = 0
        activeClick = null
    }

    /** Preenche [out] com o próximo trecho de áudio e devolve os tempos que começaram nele. */
    fun render(out: FloatArray): List<Beat> {
        val beats = mutableListOf<Beat>()
        for (i in out.indices) {
            val samplePosition = position + i
            if (samplePosition >= nextBeatAt) {
                val accent = timeSignature.accentOf(nextIndexInBar, accentEnabled)
                beats += Beat(samplePosition, nextIndexInBar, accent)
                activeClick = clicks.getValue(accent)
                activeOffset = 0
                lastBeatAt = nextBeatAt
                nextBeatAt += samplesPerBeat
                nextIndexInBar = (nextIndexInBar + 1) % timeSignature.beatsPerBar
            }
            val click = activeClick
            out[i] = if (click != null) {
                val value = click[activeOffset++]
                if (activeOffset == click.size) activeClick = null
                value
            } else {
                0f
            }
        }
        position += out.size
        return beats
    }
}
