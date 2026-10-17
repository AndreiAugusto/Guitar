package dev.andrei.violaolivre.ui.tuner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
        TunerContent(state, modifier)
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
private fun TunerContent(state: TunerUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val note = state.note
        val frequency = state.frequency
        // Leitura "segurada" (corda já parou de soar) aparece esmaecida
        val readingAlpha = if (state.isHolding) 0.4f else 1f
        Text(
            text = note?.fullName() ?: stringResource(R.string.tuner_no_note),
            fontSize = 96.sp,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.alpha(readingAlpha),
        )
        Text(
            text = if (frequency != null) {
                stringResource(R.string.tuner_reading, frequency, state.cents.roundToInt())
            } else {
                stringResource(R.string.tuner_play_string)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alpha(readingAlpha),
        )
        if (state.micError) {
            Text(
                text = stringResource(R.string.tuner_mic_error),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
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
