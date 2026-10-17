package dev.andrei.violaolivre.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class PitchSmootherTest {

    @Test
    fun `devolve a mediana das últimas leituras`() {
        val smoother = PitchSmoother(size = 5)
        listOf(110.0, 110.4, 109.8, 110.2).forEach { smoother.add(it) }
        assertEquals(110.2, smoother.add(110.3), 1e-9) // mediana de 109.8 110.0 110.2 110.3 110.4
    }

    @Test
    fun `uma leitura isolada fora da nota é ignorada`() {
        val smoother = PitchSmoother()
        repeat(4) { smoother.add(110.0) }
        assertEquals(110.0, smoother.add(220.0), 1e-9) // erro de oitava
        assertEquals(110.0, smoother.add(110.0), 1e-9)
    }

    @Test
    fun `duas leituras seguidas na nota nova trocam de nota`() {
        val smoother = PitchSmoother()
        repeat(4) { smoother.add(110.0) }
        assertEquals(110.0, smoother.add(146.8), 1e-9)
        assertEquals(146.85, smoother.add(146.9), 1e-9)
    }

    @Test
    fun `reset esquece o histórico`() {
        val smoother = PitchSmoother()
        repeat(4) { smoother.add(110.0) }
        smoother.reset()
        assertEquals(196.0, smoother.add(196.0), 1e-9)
    }
}
