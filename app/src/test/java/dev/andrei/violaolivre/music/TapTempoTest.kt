package dev.andrei.violaolivre.music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TapTempoTest {

    @Test
    fun `toques a cada meio segundo dão 120 BPM`() {
        val tap = TapTempo()
        assertNull(tap.tap(0))
        assertEquals(120, tap.tap(500))
        assertEquals(120, tap.tap(1000))
    }

    @Test
    fun `média dos últimos intervalos suaviza toques irregulares`() {
        val tap = TapTempo()
        listOf(0L, 480, 1000, 1490).forEach { tap.tap(it) }
        assertEquals(120, tap.tap(2000)) // intervalos 480 520 490 510 → média 500 ms
    }

    @Test
    fun `uma pausa longa recomeça a contagem`() {
        val tap = TapTempo()
        tap.tap(0)
        tap.tap(1000) // 60 BPM
        assertNull(tap.tap(5000))
        assertEquals(150, tap.tap(5400))
    }

    @Test
    fun `BPM fica dentro do limite`() {
        val tap = TapTempo()
        tap.tap(0)
        assertEquals(240, tap.tap(100)) // seria 600 BPM
    }
}
