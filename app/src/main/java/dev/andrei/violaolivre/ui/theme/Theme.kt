package dev.andrei.violaolivre.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Por enquanto o app é só escuro; a escolha claro/escuro fica para a Fase 5.
private val DarkColors = darkColorScheme(
    primary = Blue,
    onPrimary = OnBlue,
    primaryContainer = BlueContainer,
    onPrimaryContainer = Blue,
    secondaryContainer = BlueContainer,
    onSecondaryContainer = Blue,
    background = Background,
    onBackground = OnSurfaceDark,
    surface = Background,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainer = SurfaceContainerDark,
    outline = OutlineDark,
)

/** Envolve as telas do app e entrega a elas as cores e a tipografia. */
@Composable
fun ViolaoLivreTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography(),
        content = content,
    )
}
