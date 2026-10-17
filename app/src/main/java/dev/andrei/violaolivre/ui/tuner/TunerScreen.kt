package dev.andrei.violaolivre.ui.tuner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.music.Note
import dev.andrei.violaolivre.music.Tuning
import dev.andrei.violaolivre.music.Tunings
import dev.andrei.violaolivre.ui.theme.InTuneGreen
import dev.andrei.violaolivre.ui.theme.OutOfTuneOrange
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TunerScreen(
    modifier: Modifier = Modifier,
    viewModel: TunerViewModel = viewModel(),
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    var hasPermission by remember { mutableStateOf(isMicGranted(context)) }
    var permanentlyDenied by remember { mutableStateOf(false) }

    // Abre o diálogo do sistema "Permitir que o app grave áudio?" e recebe a resposta
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        // Depois da 2ª recusa o Android não mostra mais o diálogo: só pelas configurações
        permanentlyDenied = !granted && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)
    }

    // Ao voltar para o app (ex.: depois de mexer nas configurações), confere de novo
    LifecycleResumeEffect(Unit) {
        hasPermission = isMicGranted(context)
        onPauseOrDispose { }
    }

    if (hasPermission) {
        // Microfone ligado só enquanto esta tela está visível: desliga ao trocar de aba
        // (dispose) ou quando o app vai para segundo plano (stop)
        LifecycleStartEffect(Unit) {
            viewModel.startListening()
            onStopOrDispose { viewModel.stopListening() }
        }
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        TunerContent(
            state = state,
            onSelectTuning = viewModel::selectTuning,
            onSelectChromatic = viewModel::selectChromatic,
            onToggleString = viewModel::toggleString,
            modifier = modifier,
        )
    } else {
        MicPermissionRequest(
            permanentlyDenied = permanentlyDenied,
            onRequest = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            onOpenSettings = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri()),
                )
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun TunerContent(
    state: TunerUiState,
    onSelectTuning: (Tuning) -> Unit,
    onSelectChromatic: () -> Unit,
    onToggleString: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.tab_tuner), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.weight(1f))
            TuningSelector(state, onSelectTuning, onSelectChromatic)
        }

        Spacer(Modifier.weight(1f))

        // Esmaecido quando não é uma leitura ao vivo: corda parou de soar,
        // ou corda fixa escolhida mas ainda sem som
        val isLive = state.frequency != null && !state.isHolding
        val readingAlpha = if (isLive) 1f else 0.4f

        NoteDisplay(state.targetNote, state.useFlats, Modifier.alpha(readingAlpha))
        val frequency = state.frequency
        val cents = state.cents
        Text(
            text = if (frequency != null && cents != null) {
                stringResource(R.string.tuner_reading, frequency, cents.roundToInt())
            } else {
                ""
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alpha(readingAlpha),
        )

        Spacer(Modifier.height(24.dp))
        TuningMeter(cents, state.isInTune, needleAlpha = readingAlpha)
        Spacer(Modifier.height(24.dp))

        if (!state.isChromatic) {
            StringSelector(state, onToggleString)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(
                    if (state.manualStringIndex != null) R.string.tuner_mode_manual else R.string.tuner_mode_auto,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
        }

        TuningHint(cents, Modifier.alpha(readingAlpha))

        if (state.micError) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.tuner_mic_error),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

/** Botão com o nome da afinação; abre a lista das afinações prontas + modo cromático. */
@Composable
private fun TuningSelector(
    state: TunerUiState,
    onSelectTuning: (Tuning) -> Unit,
    onSelectChromatic: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    // Box: o menu se posiciona em relação ao botão
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(if (state.isChromatic) stringResource(R.string.tuning_chromatic) else tuningName(state.tuning))
            },
            trailingIcon = {
                Icon(
                    painterResource(R.drawable.ic_arrow_drop_down),
                    contentDescription = stringResource(R.string.tuner_choose_tuning),
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                trailingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            border = null,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Tunings.all.forEach { tuning ->
                TuningMenuItem(
                    title = tuningName(tuning),
                    subtitle = tuning.stringNames(),
                    selected = !state.isChromatic && tuning == state.tuning,
                    onClick = {
                        onSelectTuning(tuning)
                        expanded = false
                    },
                )
            }
            HorizontalDivider()
            TuningMenuItem(
                title = stringResource(R.string.tuning_chromatic),
                subtitle = stringResource(R.string.tuning_chromatic_description),
                selected = state.isChromatic,
                onClick = {
                    onSelectChromatic()
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun TuningMenuItem(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Column {
                Text(
                    title,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        onClick = onClick,
    )
}

/** Nota grande com a oitava pequena ao lado, ex.: "A" + "2". */
@Composable
private fun NoteDisplay(note: Note?, useFlats: Boolean, modifier: Modifier = Modifier) {
    // Altura fixa: o layout não "pula" quando a nota aparece ou some
    Row(modifier.height(140.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = note?.name(useFlats) ?: stringResource(R.string.tuner_no_note),
            fontSize = 96.sp,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.alignByBaseline(),
        )
        if (note != null) {
            Text(
                text = note.octave.toString(),
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}

/** As 6 cordas da afinação; a corda alvo fica destacada e tocar numa fixa a escolha. */
@Composable
private fun StringSelector(state: TunerUiState, onToggleString: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        state.tuning.notes.forEachIndexed { index, note ->
            val isTarget = state.targetStringIndex == index
            val isManual = state.manualStringIndex == index
            val name = note.name(state.tuning.useFlats)
            val description = stringResource(R.string.tuner_string_description, 6 - index, name)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isTarget) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .then(
                        if (isManual) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier,
                    )
                    .clickable { onToggleString(index) }
                    .semantics { contentDescription = description },
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = when {
                        isTarget && state.isInTune -> InTuneGreen
                        isTarget -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
    }
}

/** Instrução em palavras: apertar, afrouxar ou "afinada!". */
@Composable
private fun TuningHint(cents: Double?, modifier: Modifier = Modifier) {
    val (text, color) = when {
        cents == null -> R.string.tuner_hint_play to MaterialTheme.colorScheme.onSurfaceVariant
        abs(cents) <= IN_TUNE_CENTS -> R.string.tuner_hint_in_tune to InTuneGreen
        cents < 0 -> R.string.tuner_hint_low to OutOfTuneOrange
        else -> R.string.tuner_hint_high to OutOfTuneOrange
    }
    Text(
        stringResource(text),
        style = MaterialTheme.typography.titleMedium,
        color = color,
        modifier = modifier,
    )
}

@Composable
private fun MicPermissionRequest(
    permanentlyDenied: Boolean,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.tuner_permission_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(if (permanentlyDenied) R.string.tuner_permission_denied else R.string.tuner_permission_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (permanentlyDenied) {
            Button(onClick = onOpenSettings) { Text(stringResource(R.string.tuner_open_settings)) }
        } else {
            Button(onClick = onRequest) { Text(stringResource(R.string.tuner_permission_button)) }
        }
    }
}

private fun isMicGranted(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED
