package dev.andrei.violaolivre.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Gera o som de uma corda dedilhada por síntese aditiva: soma a fundamental e alguns
 * harmônicos (múltiplos exatos da frequência), cada um sumindo aos poucos.
 * Os harmônicos agudos somem mais rápido, como numa corda de verdade.
 *
 * Kotlin puro, para poder ser testado no computador.
 */
object StringSynth {

    private const val HARMONICS = 8
    private const val ATTACK_SECONDS = 0.005 // 5 ms de subida evita um "clique" no início
    private const val DECAY_PER_SECOND = 1.2

    fun pluck(
        frequency: Double,
        sampleRate: Int = MicListener.SAMPLE_RATE,
        durationSeconds: Double = 3.0,
    ): FloatArray {
        val samples = FloatArray((sampleRate * durationSeconds).toInt())
        for (i in samples.indices) {
            val t = i.toDouble() / sampleRate
            var value = 0.0
            for (h in 1..HARMONICS) {
                val harmonicHz = frequency * h
                if (harmonicHz >= sampleRate / 2) break // acima do limite que o áudio consegue representar
                val amplitude = 1.0 / h
                val decay = exp(-t * DECAY_PER_SECOND * (1 + 0.6 * (h - 1)))
                value += amplitude * decay * sin(2 * PI * harmonicHz * t)
            }
            val attack = min(1.0, t / ATTACK_SECONDS)
            samples[i] = (value * attack).toFloat()
        }
        normalize(samples, peak = 0.8f)
        return samples
    }

    /** Ajusta o volume para o pico ficar em [peak] (abaixo de 1, para não distorcer). */
    private fun normalize(samples: FloatArray, peak: Float) {
        val max = samples.maxOf { abs(it) }
        if (max == 0f) return
        val gain = peak / max
        for (i in samples.indices) samples[i] *= gain
    }
}
