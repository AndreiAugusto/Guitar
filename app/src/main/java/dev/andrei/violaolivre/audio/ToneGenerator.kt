package dev.andrei.violaolivre.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Toca a nota de referência pelo alto-falante. */
class ToneGenerator {

    private var track: AudioTrack? = null

    /** Gera o som de corda em [frequency] Hz e toca (interrompe o som anterior, se houver). */
    suspend fun play(frequency: Double) = playSamples { StringSynth.pluck(frequency) }

    /** Toca um acorde dedilhado (frequências da corda mais grave para a mais aguda). */
    suspend fun playChord(frequencies: List<Double>) = playSamples { StringSynth.strum(frequencies) }

    private suspend fun playSamples(synthesize: () -> FloatArray) {
        // A síntese leva alguns milissegundos: fora da thread da interface
        val samples = withContext(Dispatchers.Default) { synthesize() }
        stop()
        // MODE_STATIC: o som inteiro é entregue de uma vez (bom para sons curtos e prontos)
        val newTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(MicListener.SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .build(),
            )
            .setBufferSizeInBytes(samples.size * Float.SIZE_BYTES)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        newTrack.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
        newTrack.play()
        track = newTrack
    }

    /** Para o som e libera o alto-falante. */
    fun stop() {
        track?.release()
        track = null
    }
}
