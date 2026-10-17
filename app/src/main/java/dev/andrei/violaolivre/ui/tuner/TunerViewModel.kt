package dev.andrei.violaolivre.ui.tuner

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.andrei.violaolivre.audio.MicListener
import dev.andrei.violaolivre.audio.PitchDetector
import dev.andrei.violaolivre.audio.PitchSmoother
import dev.andrei.violaolivre.music.Note
import dev.andrei.violaolivre.music.centsBetween
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tudo o que a tela do afinador precisa para se desenhar. */
data class TunerUiState(
    /** Frequência suavizada em Hz; null quando nenhuma corda está soando. */
    val frequency: Double? = null,
    /** true quando a corda parou de soar e a tela só está mantendo a última leitura. */
    val isHolding: Boolean = false,
    val micError: Boolean = false,
) {
    val note: Note? get() = frequency?.let(Note::nearest)

    /** Desvio em cents até a nota mais próxima. */
    val cents: Double get() = frequency?.let { centsBetween(it, Note.nearest(it).frequency) } ?: 0.0
}

/**
 * Cérebro do afinador: liga o microfone, passa o áudio pelo detector e publica o estado.
 * Sobrevive a rotações de tela e trocas de aba; a tela só observa [uiState].
 */
class TunerViewModel : ViewModel() {

    private val micListener = MicListener()
    private val detector = PitchDetector(MicListener.SAMPLE_RATE, MicListener.WINDOW_SIZE)
    private val smoother = PitchSmoother()
    private var silentWindows = 0

    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()

    private var listening: Job? = null

    // A tela só chama este método depois de a permissão do microfone ser concedida
    @SuppressLint("MissingPermission")
    fun startListening() {
        if (listening?.isActive == true) return
        _uiState.update { it.copy(micError = false) }
        listening = viewModelScope.launch {
            micListener.windows()
                .conflate() // se o detector atrasar, pula janelas velhas em vez de acumular
                .map { detector.detect(it) }
                .flowOn(Dispatchers.Default) // cálculo pesado fora da thread da interface
                .catch { _uiState.update { it.copy(micError = true) } }
                .collect(::onDetection)
        }
    }

    fun stopListening() {
        listening?.cancel()
        listening = null
        smoother.reset()
        silentWindows = 0
        _uiState.update { it.copy(frequency = null, isHolding = false) }
    }

    private fun onDetection(result: PitchDetector.Result?) {
        if (result == null) {
            // Mantém a última nota por um instante enquanto a corda perde volume
            silentWindows++
            if (silentWindows == 1) {
                _uiState.update { it.copy(isHolding = it.frequency != null) }
            } else if (silentWindows == SILENCE_WINDOWS) {
                smoother.reset()
                _uiState.update { it.copy(frequency = null, isHolding = false) }
            }
            return
        }
        silentWindows = 0
        val frequency = smoother.add(result.frequency)
        _uiState.update { it.copy(frequency = frequency, isHolding = false, micError = false) }
    }

    private companion object {
        // ~2,5 segundos sem nota (cada janela nova chega a cada ~46 ms)
        const val SILENCE_WINDOWS = 54
    }
}
