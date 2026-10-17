package dev.andrei.violaolivre.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import dev.andrei.violaolivre.music.TimeSignature
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Toca o metrônomo: uma thread de áudio pede ao [ClickRenderer] o próximo pedaço de som
 * e entrega ao AudioTrack em modo streaming. O `write` bloqueia quando o buffer do
 * alto-falante está cheio, então é o próprio áudio que dita o ritmo do loop.
 */
class MetronomeEngine {

    var bpm: Int = 100
        set(value) {
            field = value
            withRenderer { it.bpm = value }
        }

    var timeSignature: TimeSignature = TimeSignature.FOUR_FOUR
        set(value) {
            field = value
            withRenderer { it.timeSignature = value }
        }

    var accentEnabled: Boolean = true
        set(value) {
            field = value
            withRenderer { it.accentEnabled = value }
        }

    /**
     * Uma execução (do play ao stop). Cada play cria uma nova, então um stop seguido de
     * play rápido não mistura a thread velha (ainda terminando) com a nova.
     */
    private class Session(val renderer: ClickRenderer) {
        @Volatile var running = true
        // Quantas amostras o alto-falante já tocou (atualizado pela thread de áudio)
        @Volatile var playedSamples = 0L
        // Tempos já escritos no buffer, esperando o alto-falante chegar neles
        val pendingBeats = ConcurrentLinkedQueue<ClickRenderer.Beat>()
    }

    @Volatile private var session: Session? = null

    val isPlaying: Boolean get() = session != null

    fun start() {
        if (session != null) return
        val renderer = ClickRenderer(SAMPLE_RATE).also {
            it.bpm = bpm
            it.timeSignature = timeSignature
            it.accentEnabled = accentEnabled
        }
        val newSession = Session(renderer)
        session = newSession
        Thread({ runAudioLoop(newSession) }, "metronomo").start()
    }

    fun stop() {
        // Só avisa: a própria thread de áudio fecha o AudioTrack ao sair do loop
        session?.running = false
        session = null
    }

    /**
     * O tempo que acabou de soar no alto-falante (não o que acabou de ser escrito no
     * buffer, que está ~100 ms adiantado), ou null se nenhum tempo novo começou.
     */
    fun pollSoundingBeat(): ClickRenderer.Beat? {
        val current = session ?: return null
        var latest: ClickRenderer.Beat? = null
        while (true) {
            val beat = current.pendingBeats.peek() ?: break
            if (beat.samplePosition > current.playedSamples) break
            latest = current.pendingBeats.poll()
        }
        return latest
    }

    private fun withRenderer(change: (ClickRenderer) -> Unit) {
        val renderer = session?.renderer ?: return
        synchronized(renderer) { change(renderer) }
    }

    private fun runAudioLoop(session: Session) {
        // Prioridade de áudio: o sistema dá preferência a esta thread, evitando falhas no som
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        val minBuffer = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .build(),
            )
            .setBufferSizeInBytes(maxOf(minBuffer, CHUNK_SIZE * Float.SIZE_BYTES * 2))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        val buffer = FloatArray(CHUNK_SIZE)
        try {
            track.play()
            while (session.running) {
                val beats = synchronized(session.renderer) { session.renderer.render(buffer) }
                session.pendingBeats.addAll(beats)
                track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
                // playbackHeadPosition é um contador de 32 bits sem sinal
                session.playedSamples = track.playbackHeadPosition.toLong() and 0xFFFFFFFFL
            }
        } finally {
            track.stop()
            track.release()
        }
    }

    private companion object {
        const val SAMPLE_RATE = 44100
        // ~23 ms por pedaço: mudanças de BPM e o fim do som respondem rápido
        const val CHUNK_SIZE = 1024
    }
}
