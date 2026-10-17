package dev.andrei.violaolivre.ui.theory

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.ui.components.PlaceholderScreen

@Composable
fun TheoryScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.tab_theory),
        message = stringResource(R.string.placeholder_theory),
        modifier = modifier,
    )
}
