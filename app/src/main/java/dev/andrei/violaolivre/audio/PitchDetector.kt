package dev.andrei.violaolivre.audio

import kotlin.math.sqrt

/**
 * Detecta a frequência fundamental de um trecho de áudio com o algoritmo YIN
 * (de Cheveigné e Kawahara, 2002).
 *
 * A ideia: um som afinado é periódico, ou seja, se repete a cada T amostras.
 * O YIN compara o sinal com ele mesmo deslocado de τ amostras e procura o menor τ
 * em que as duas cópias quase coincidem. A frequência é então sampleRate / τ.
 *
 * Kotlin puro (sem Android), para poder ser testado no computador.
 *
 * @param sampleRate amostras por segundo do áudio (ex.: 44100)
 * @param bufferSize quantas amostras cada chamada de [detect] recebe
 * @param minFrequency nota mais grave procurada, em Hz (define o maior τ)
 * @param maxFrequency nota mais aguda procurada, em Hz (define o menor τ)
 * @param threshold limiar do passo 3: quanto menor, mais exigente com a periodicidade
 * @param minRms volume mínimo; abaixo disso o trecho é tratado como silêncio
 */
class PitchDetector(
    private val sampleRate: Int,
    private val bufferSize: Int,
    minFrequency: Double = 60.0,
    maxFrequency: Double = 1200.0,
    private val threshold: Double = 0.15,
    private val minRms: Double = 0.01,
) {
    /** Frequência encontrada e confiança (0 a 1; quanto mais perto de 1, mais periódico o som). */
    data class Result(val frequency: Double, val confidence: Double)

    private val minTau = (sampleRate / maxFrequency).toInt().coerceAtLeast(2)
    private val maxTau = (sampleRate / minFrequency).toInt()

    // Tamanho da janela comparada: o que sobra do buffer depois do maior deslocamento
    private val window = bufferSize - (maxTau + 1)

    // Alocados uma vez só: detect() roda dezenas de vezes por segundo
    private val difference = DoubleArray(maxTau + 2)
    private val normalized = DoubleArray(maxTau + 2)

    init {
        require(window > maxTau) { "bufferSize pequeno demais para minFrequency" }
    }

    /** Devolve a frequência de [samples] (valores entre -1 e 1) ou null se não houver nota clara. */
    fun detect(samples: FloatArray): Result? {
        require(samples.size >= bufferSize) { "Esperava $bufferSize amostras" }
        if (rms(samples) < minRms) return null

        // Passo 1 — função diferença: d(τ) = Σ (x[j] - x[j+τ])²
        // É pequena quando o sinal deslocado de τ "encaixa" no original.
        for (tau in 1..maxTau + 1) {
            var sum = 0.0
            for (j in 0 until window) {
                val delta = samples[j] - samples[j + tau]
                sum += delta * delta
            }
            difference[tau] = sum
        }

        // Passo 2 — diferença normalizada pela média acumulada.
        // Evita escolher τ muito pequeno (onde d(τ) é baixo só porque o deslocamento é mínimo).
        normalized[0] = 1.0
        var runningSum = 0.0
        for (tau in 1..maxTau + 1) {
            runningSum += difference[tau]
            normalized[tau] = if (runningSum == 0.0) 1.0 else difference[tau] * tau / runningSum
        }

        // Passo 3 — limiar absoluto: o primeiro τ abaixo do limiar, descendo até o fundo do "vale".
        // Pegar o primeiro (e não o menor de todos) evita erros de oitava para baixo.
        var tau = minTau
        var bestTau = -1
        while (tau <= maxTau) {
            if (normalized[tau] < threshold) {
                while (tau + 1 <= maxTau && normalized[tau + 1] < normalized[tau]) tau++
                bestTau = tau
                break
            }
            tau++
        }
        if (bestTau == -1) return null

        // Passo 4 — interpolação parabólica: estima o mínimo "entre" duas amostras,
        // o que dá precisão bem menor que 1 amostra (essencial para medir cents).
        val refinedTau = parabolicMinimum(bestTau)
        return Result(
            frequency = sampleRate / refinedTau,
            confidence = 1.0 - normalized[bestTau],
        )
    }

    private fun parabolicMinimum(tau: Int): Double {
        val left = normalized[tau - 1]
        val center = normalized[tau]
        val right = normalized[tau + 1]
        val denominator = left - 2 * center + right
        if (denominator == 0.0) return tau.toDouble()
        return tau + (left - right) / (2 * denominator)
    }

    private fun rms(samples: FloatArray): Double {
        var sum = 0.0
        for (i in 0 until bufferSize) sum += samples[i] * samples[i]
        return sqrt(sum / bufferSize)
    }
}
