package dev.andrei.violaolivre.audio

import dev.andrei.violaolivre.music.Tunings
import dev.andrei.violaolivre.music.centsBetween
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class PitchDetectorTest {

    private val sampleRate = 44100
    private val bufferSize = 4096
    private val detector = PitchDetector(sampleRate, bufferSize)

    /**
     * Gera um sinal sintético: a fundamental mais harmônicos (múltiplos inteiros da frequência)
     * com as amplitudes dadas, e um pouco de ruído aleatório.
     */
    private fun signal(
        frequency: Double,
        harmonics: List<Double> = listOf(1.0),
        noise: Double = 0.0,
        amplitude: Double = 0.5,
    ): FloatArray {
        val random = Random(42)
        val total = harmonics.sum()
        return FloatArray(bufferSize) { i ->
            val t = i.toDouble() / sampleRate
            var value = 0.0
            harmonics.forEachIndexed { h, weight ->
                value += weight * sin(2 * PI * frequency * (h + 1) * t + h)
            }
            (amplitude * value / total + noise * (random.nextDouble() * 2 - 1)).toFloat()
        }
    }

    private fun assertDetects(expectedHz: Double, samples: FloatArray, toleranceCents: Double = 1.0) {
        val result = detector.detect(samples)
        assertNotNull("nenhuma nota detectada em $expectedHz Hz", result)
        val error = centsBetween(result!!.frequency, expectedHz)
        assertTrue(
            "esperava $expectedHz Hz, detectou ${result.frequency} Hz (erro de $error cents)",
            abs(error) <= toleranceCents,
        )
    }

    @Test
    fun `senoide pura em cada corda solta da afinação padrão`() {
        Tunings.STANDARD.notes.forEach { note ->
            assertDetects(note.frequency, signal(note.frequency))
        }
    }

    @Test
    fun `som com harmônicos e ruído, parecido com uma corda`() {
        val guitarLike = listOf(1.0, 0.8, 0.5, 0.35, 0.2, 0.1)
        Tunings.STANDARD.notes.forEach { note ->
            assertDetects(note.frequency, signal(note.frequency, guitarLike, noise = 0.03))
        }
    }

    @Test
    fun `fundamental fraca não vira erro de oitava`() {
        // Na corda mais grave, o 2º harmônico costuma ser mais forte que a fundamental
        val weakFundamental = listOf(0.3, 1.0, 0.6, 0.4)
        assertDetects(82.41, signal(82.41, weakFundamental, noise = 0.02))
        assertDetects(73.42, signal(73.42, weakFundamental, noise = 0.02)) // D2 do Drop D
    }

    @Test
    fun `corda desafinada é medida com precisão`() {
        assertDetects(84.0, signal(84.0, listOf(1.0, 0.6, 0.3)))    // E2 alta (+33 cents)
        assertDetects(242.0, signal(242.0, listOf(1.0, 0.6, 0.3)))  // B3 baixa (-35 cents)
    }

    @Test
    fun `afinação mais grave do app (C2 do Drop C)`() {
        assertDetects(65.41, signal(65.41, listOf(1.0, 0.7, 0.4)))
    }

    @Test
    fun `silêncio não tem nota`() {
        assertNull(detector.detect(FloatArray(bufferSize)))
        assertNull(detector.detect(signal(110.0, amplitude = 0.001)))
    }

    @Test
    fun `ruído puro não tem nota`() {
        val random = Random(7)
        val noise = FloatArray(bufferSize) { (random.nextDouble() * 2 - 1).toFloat() * 0.3f }
        assertNull(detector.detect(noise))
    }

    @Test
    fun `confiança alta para senoide pura`() {
        val result = detector.detect(signal(110.0))!!
        assertEquals(1.0, result.confidence, 0.05)
    }
}
