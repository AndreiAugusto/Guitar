package dev.andrei.violaolivre.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

/**
 * Captura contínua do microfone.
 *
 * Entrega "janelas" de [WINDOW_SIZE] amostras que se sobrepõem: a cada [HOP_SIZE] amostras
 * novas (~46 ms) sai uma janela, então o detector roda ~21 vezes por segundo, mas sempre
 * olhando ~93 ms de som (o bastante para a corda mais grave repetir várias vezes).
 */
class MicListener {

    /**
     * Um Flow que lê o microfone enquanto alguém estiver coletando.
     * Quando a coleta é cancelada (ex.: saiu da tela), o microfone é liberado no `finally`.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun windows(): Flow<FloatArray> = flow {
        val minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
        val record = AudioRecord(
            // VOICE_RECOGNITION: entrada "crua", sem controle automático de volume nem redução de ruído
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SAMPLE_RATE,
            CHANNEL,
            ENCODING,
            maxOf(minBuffer, WINDOW_SIZE * Float.SIZE_BYTES),
        )
        check(record.state == AudioRecord.STATE_INITIALIZED) { "Microfone indisponível" }

        val window = FloatArray(WINDOW_SIZE)
        val hop = FloatArray(HOP_SIZE)
        try {
            record.startRecording()
            while (currentCoroutineContext().isActive) {
                // Lê exatamente HOP_SIZE amostras novas (read pode devolver menos de uma vez)
                var read = 0
                while (read < HOP_SIZE) {
                    val count = record.read(hop, read, HOP_SIZE - read, AudioRecord.READ_BLOCKING)
                    check(count >= 0) { "Erro ao ler o microfone: $count" }
                    read += count
                }
                // Desliza a janela: descarta as amostras mais antigas e põe as novas no fim
                System.arraycopy(window, HOP_SIZE, window, 0, WINDOW_SIZE - HOP_SIZE)
                System.arraycopy(hop, 0, window, WINDOW_SIZE - HOP_SIZE, HOP_SIZE)
                emit(window.copyOf())
            }
        } finally {
            record.stop()
            record.release()
        }
    }.flowOn(Dispatchers.IO) // a leitura bloqueia, então roda numa thread de I/O

    companion object {
        const val SAMPLE_RATE = 44100
        const val WINDOW_SIZE = 4096
        const val HOP_SIZE = 2048
        private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        // Amostras como Float entre -1 e 1, o formato que o PitchDetector espera
        private const val ENCODING = AudioFormat.ENCODING_PCM_FLOAT
    }
}
