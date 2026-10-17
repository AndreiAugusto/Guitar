package dev.andrei.violaolivre.ui.songs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.ui.components.PlaceholderScreen

@Composable
fun SongsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.tab_songs),
        message = stringResource(R.string.placeholder_songs),
        modifier = modifier,
    )
}
