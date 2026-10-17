package dev.andrei.violaolivre.audio

import dev.andrei.violaolivre.music.centsBetween
import kotlin.math.abs

/**
 * Suaviza as leituras do detector para a nota na tela não ficar "tremendo".
 *
 * Mostra a mediana das últimas [size] leituras. Um salto grande (mais de [jumpCents]),
 * como trocar de corda, só é aceito se a leitura seguinte confirmar; assim um erro
 * isolado (ex.: uma leitura uma oitava acima) é ignorado.
 */
class PitchSmoother(private val size: Int = 5, private val jumpCents: Double = 50.0) {

    private val history = ArrayDeque<Double>()
    private var candidate: Double? = null

    /** Adiciona uma leitura e devolve a frequência suavizada. */
    fun add(frequency: Double): Double {
        val current = median()
        if (current == null || isClose(frequency, current)) {
            history.addLast(frequency)
            if (history.size > size) history.removeFirst()
            candidate = null
        } else {
            val pending = candidate
            if (pending != null && isClose(frequency, pending)) {
                // Duas leituras seguidas na nota nova: troca de nota
                history.clear()
                history.addLast(pending)
                history.addLast(frequency)
                candidate = null
            } else {
                candidate = frequency
            }
        }
        return median()!!
    }

    fun reset() {
        history.clear()
        candidate = null
    }

    private fun isClose(a: Double, b: Double) = abs(centsBetween(a, b)) <= jumpCents

    private fun median(): Double? {
        if (history.isEmpty()) return null
        val sorted = history.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 1) sorted[middle] else (sorted[middle - 1] + sorted[middle]) / 2
    }
}
