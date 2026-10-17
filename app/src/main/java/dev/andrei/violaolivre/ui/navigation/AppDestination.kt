package dev.andrei.violaolivre.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.andrei.violaolivre.R
import kotlinx.serialization.Serializable

// Rotas tipadas: cada tela é identificada por um tipo, não por uma string.
// @Serializable deixa o Navigation transformar a rota (e argumentos futuros) em texto.
@Serializable data object TunerRoute
@Serializable data object SongsRoute
@Serializable data object TheoryRoute
@Serializable data object MetronomeRoute

/** As abas da barra inferior, na ordem em que aparecem. */
enum class AppDestination(
    val route: Any,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
) {
    TUNER(TunerRoute, R.string.tab_tuner, R.drawable.ic_tab_tuner),
    SONGS(SongsRoute, R.string.tab_songs, R.drawable.ic_tab_songs),
    THEORY(TheoryRoute, R.string.tab_theory, R.drawable.ic_tab_theory),
    METRONOME(MetronomeRoute, R.string.tab_metronome, R.drawable.ic_tab_metronome),
}
