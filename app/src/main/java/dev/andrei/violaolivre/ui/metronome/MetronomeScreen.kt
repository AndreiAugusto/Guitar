package dev.andrei.violaolivre.ui.metronome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.ui.components.PlaceholderScreen

@Composable
fun MetronomeScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.tab_metronome),
        message = stringResource(R.string.placeholder_metronome),
        modifier = modifier,
    )
}
