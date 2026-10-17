package dev.andrei.violaolivre.ui.metronome

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.music.Accent
import dev.andrei.violaolivre.music.TimeSignature
import kotlin.math.roundToInt

/** [viewModel] vem do AppNavHost (escopo da Activity), para o som continuar entre abas. */
@Composable
fun MetronomeScreen(viewModel: MetronomeViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MetronomeContent(
        state = state,
        onTogglePlaying = viewModel::togglePlaying,
        onBpmChange = viewModel::setBpm,
        onBpmStep = viewModel::changeBpm,
        onTimeSignatureChange = viewModel::selectTimeSignature,
        onAccentChange = viewModel::setAccentEnabled,
        onTap = viewModel::tap,
        modifier = modifier,
    )
}

@Composable
private fun MetronomeContent(
    state: MetronomeUiState,
    onTogglePlaying: () -> Unit,
    onBpmChange: (Int) -> Unit,
    onBpmStep: (Int) -> Unit,
    onTimeSignatureChange: (TimeSignature) -> Unit,
    onAccentChange: (Boolean) -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.tab_metronome),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.weight(1f))

        BeatIndicator(state)

        Spacer(Modifier.height(32.dp))

        Text(state.bpm.toString(), fontSize = 96.sp, style = MaterialTheme.typography.displayLarge)
        Text(
            stringResource(R.string.metronome_bpm),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = { onBpmStep(-1) }) {
                Text("−", style = MaterialTheme.typography.titleLarge)
            }
            Slider(
                value = state.bpm.toFloat(),
                onValueChange = { onBpmChange(it.roundToInt()) },
                valueRange = MetronomeUiState.BPM_RANGE.first.toFloat()..MetronomeUiState.BPM_RANGE.last.toFloat(),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            )
            FilledTonalIconButton(onClick = { onBpmStep(1) }) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Botões "segmentados": um grupo em que só uma opção fica marcada
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            TimeSignature.entries.forEachIndexed { index, signature ->
                SegmentedButton(
                    selected = signature == state.timeSignature,
                    onClick = { onTimeSignatureChange(signature) },
                    shape = SegmentedButtonDefaults.itemShape(index, TimeSignature.entries.size),
                ) {
                    Text(signature.label)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.metronome_accent), modifier = Modifier.weight(1f))
            Switch(checked = state.accentEnabled, onCheckedChange = onAccentChange)
        }

        Spacer(Modifier.weight(1f))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = onTap, modifier = Modifier.weight(1f).height(56.dp)) {
                Text(stringResource(R.string.metronome_tap))
            }
            Button(onClick = onTogglePlaying, modifier = Modifier.weight(1f).height(56.dp)) {
                Icon(
                    painterResource(if (state.isPlaying) R.drawable.ic_stop else R.drawable.ic_play_arrow),
                    contentDescription = null,
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (state.isPlaying) R.string.metronome_stop else R.string.metronome_play))
            }
        }
    }
}

/** Uma bolinha por tempo do compasso; a do tempo que está soando acende e cresce. */
@Composable
private fun BeatIndicator(state: MetronomeUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(state.timeSignature.beatsPerBar) { beat ->
            val accent = state.timeSignature.accentOf(beat, state.accentEnabled)
            val isCurrent = state.currentBeat == beat
            val activeColor = when (accent) {
                Accent.STRONG -> MaterialTheme.colorScheme.primary
                Accent.MEDIUM -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                Accent.NONE -> MaterialTheme.colorScheme.onSurface
            }
            val color by animateColorAsState(
                if (isCurrent) activeColor else MaterialTheme.colorScheme.outline,
                label = "cor do tempo",
            )
            val scale by animateFloatAsState(if (isCurrent) 1.3f else 1f, label = "tamanho do tempo")
            Spacer(
                Modifier
                    .size(if (accent == Accent.NONE) 20.dp else 26.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color)
                    .align(Alignment.CenterVertically),
            )
        }
    }
}
