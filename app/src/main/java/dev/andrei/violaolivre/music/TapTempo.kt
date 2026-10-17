package dev.andrei.violaolivre.music

import kotlin.math.roundToInt

/**
 * Calcula o BPM a partir de toques no ritmo da música.
 * Usa a média dos últimos intervalos; uma pausa longa recomeça a contagem.
 */
class TapTempo(
    private val maxIntervals: Int = 4,
    private val resetAfterMillis: Long = 2000,
    private val bpmRange: IntRange = 40..240,
) {
    private val intervals = ArrayDeque<Long>()
    private var lastTapMillis: Long? = null

    /** Registra um toque no instante [nowMillis]; devolve o BPM a partir do 2º toque. */
    fun tap(nowMillis: Long): Int? {
        val last = lastTapMillis
        lastTapMillis = nowMillis
        if (last == null || nowMillis - last > resetAfterMillis) {
            intervals.clear()
            return null
        }
        intervals.addLast(nowMillis - last)
        if (intervals.size > maxIntervals) intervals.removeFirst()
        val bpm = (60_000.0 / intervals.average()).roundToInt()
        return bpm.coerceIn(bpmRange)
    }
}
