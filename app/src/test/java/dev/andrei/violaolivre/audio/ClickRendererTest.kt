package dev.andrei.violaolivre.audio

import dev.andrei.violaolivre.music.Accent
import dev.andrei.violaolivre.music.TimeSignature
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.ceil

class ClickRendererTest {

    private val sampleRate = 44100

    /** Renderiza [seconds] de áudio em pedaços de [chunk] amostras e junta os tempos. */
    private fun ClickRenderer.beatsFor(seconds: Double, chunk: Int = 1024): List<ClickRenderer.Beat> {
        val buffer = FloatArray(chunk)
        val beats = mutableListOf<ClickRenderer.Beat>()
        repeat((seconds * sampleRate / chunk).toInt()) { beats += render(buffer) }
        return beats
    }

    @Test
    fun `120 BPM em 4 por 4 - um tempo a cada meio segundo, acento no 1`() {
        val renderer = ClickRenderer(sampleRate).apply { bpm = 120 }
        val beats = renderer.beatsFor(3.1)
        assertEquals((0..6).map { it * 22050L }, beats.map { it.samplePosition })
        assertEquals(listOf(0, 1, 2, 3, 0, 1, 2), beats.map { it.indexInBar })
        assertEquals(
            listOf(Accent.STRONG, Accent.NONE, Accent.NONE, Accent.NONE, Accent.STRONG),
            beats.take(5).map { it.accent },
        )
    }

    @Test
    fun `sem deriva depois de 10 minutos, mesmo com BPM que não divide exato`() {
        val renderer = ClickRenderer(sampleRate).apply { bpm = 97 }
        val samplesPerBeat = sampleRate * 60.0 / 97
        val beats = renderer.beatsFor(600.0)
        assertEquals(970, beats.size)
        beats.forEachIndexed { n, beat ->
            val expected = ceil(n * samplesPerBeat).toLong()
            assertTrue("tempo $n em ${beat.samplePosition}, esperado $expected", abs(beat.samplePosition - expected) <= 1)
        }
    }

    @Test
    fun `o clique sai inteiro mesmo atravessando pedaços de áudio`() {
        val clicks = ClickSynth.allClicks(sampleRate)
        val renderer = ClickRenderer(sampleRate, clicks)
        // Pedaços de 100 amostras: o clique (~1300 amostras) atravessa vários deles
        val output = (0 until 20).flatMap { FloatArray(100).also(renderer::render).toList() }
        val strong = clicks.getValue(Accent.STRONG)
        assertArrayEquals(strong, output.take(strong.size).toFloatArray(), 0f)
        assertTrue(output.drop(strong.size).all { it == 0f }) // depois dele, silêncio
    }

    @Test
    fun `sem acento todos os tempos soam iguais`() {
        val renderer = ClickRenderer(sampleRate).apply { accentEnabled = false }
        assertTrue(renderer.beatsFor(3.0).all { it.accent == Accent.NONE })
    }

    @Test
    fun `6 por 8 tem acento forte no 1 e leve no 4`() {
        val renderer = ClickRenderer(sampleRate).apply { timeSignature = TimeSignature.SIX_EIGHT }
        val accents = renderer.beatsFor(3.1).take(7).map { it.accent }
        assertEquals(
            listOf(Accent.STRONG, Accent.NONE, Accent.NONE, Accent.MEDIUM, Accent.NONE, Accent.NONE, Accent.STRONG),
            accents,
        )
    }

    @Test
    fun `aumentar o BPM vale já, sem esperar o intervalo antigo`() {
        val renderer = ClickRenderer(sampleRate).apply { bpm = 40 } // um tempo a cada 1,5 s
        val buffer = FloatArray(sampleRate)                          // pedaços de 1 s
        assertEquals(listOf(0L), renderer.render(buffer).map { it.samplePosition })
        renderer.bpm = 240                                           // um tempo a cada 0,25 s
        // O tempo seguinte sai no início do próximo pedaço (1 s), não em 1,5 s
        val next = renderer.render(buffer).map { it.samplePosition }
        assertEquals(44100L, next.first())
        assertEquals(11025L, next[1] - next[0])
    }

    @Test
    fun `trocar o compasso recomeça no tempo 1`() {
        val renderer = ClickRenderer(sampleRate).apply { bpm = 120 }
        renderer.render(FloatArray(30000)) // tempos 1 e 2 do 4/4
        renderer.timeSignature = TimeSignature.THREE_FOUR
        val beats = renderer.beatsFor(2.0)
        assertEquals(listOf(0, 1, 2, 0), beats.take(4).map { it.indexInBar })
    }
}
