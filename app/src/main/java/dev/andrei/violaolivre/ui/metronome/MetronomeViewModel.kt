package dev.andrei.violaolivre.ui.metronome

import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.andrei.violaolivre.audio.MetronomeEngine
import dev.andrei.violaolivre.music.TapTempo
import dev.andrei.violaolivre.music.TimeSignature
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MetronomeUiState(
    val bpm: Int = 100,
    val timeSignature: TimeSignature = TimeSignature.FOUR_FOUR,
    val accentEnabled: Boolean = true,
    val isPlaying: Boolean = false,
    /** Tempo do compasso que está soando (0 = primeiro); null parado. */
    val currentBeat: Int? = null,
) {
    companion object {
        val BPM_RANGE = 40..240
    }
}

/**
 * Estado e controle do metrônomo. Fica no escopo da Activity (criado no AppNavHost),
 * então continua tocando ao trocar de aba; para quando o app vai para segundo plano.
 */
class MetronomeViewModel : ViewModel() {

    private val engine = MetronomeEngine()
    private val tapTempo = TapTempo(bpmRange = MetronomeUiState.BPM_RANGE)

    private val _uiState = MutableStateFlow(MetronomeUiState())
    val uiState: StateFlow<MetronomeUiState> = _uiState.asStateFlow()

    private var beatWatcher: Job? = null

    // O ProcessLifecycleOwner representa o app inteiro: onStop = nenhuma tela do app visível.
    // (Diferente do ciclo de vida da Activity, ele não dispara ao girar o celular.)
    private val appLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) = stop()
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    fun togglePlaying() {
        if (_uiState.value.isPlaying) stop() else start()
    }

    fun setBpm(bpm: Int) {
        val value = bpm.coerceIn(MetronomeUiState.BPM_RANGE)
        engine.bpm = value
        _uiState.update { it.copy(bpm = value) }
    }

    fun changeBpm(delta: Int) = setBpm(_uiState.value.bpm + delta)

    fun selectTimeSignature(timeSignature: TimeSignature) {
        engine.timeSignature = timeSignature
        _uiState.update { it.copy(timeSignature = timeSignature) }
    }

    fun setAccentEnabled(enabled: Boolean) {
        engine.accentEnabled = enabled
        _uiState.update { it.copy(accentEnabled = enabled) }
    }

    /** Um toque no botão "Tap tempo". */
    fun tap() {
        tapTempo.tap(SystemClock.uptimeMillis())?.let(::setBpm)
    }

    private fun start() {
        engine.start()
        _uiState.update { it.copy(isPlaying = true) }
        // Confere ~60 vezes por segundo qual tempo está soando, para acender a bolinha certa
        beatWatcher = viewModelScope.launch {
            while (isActive) {
                engine.pollSoundingBeat()?.let { beat ->
                    _uiState.update { it.copy(currentBeat = beat.indexInBar) }
                }
                delay(16)
            }
        }
    }

    private fun stop() {
        engine.stop()
        beatWatcher?.cancel()
        beatWatcher = null
        _uiState.update { it.copy(isPlaying = false, currentBeat = null) }
    }

    override fun onCleared() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
        engine.stop()
    }
}
