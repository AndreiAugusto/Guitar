package dev.andrei.violaolivre.ui.tuner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.ui.components.PlaceholderScreen

@Composable
fun TunerScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.tab_tuner),
        message = stringResource(R.string.placeholder_tuner),
        modifier = modifier,
    )
}
