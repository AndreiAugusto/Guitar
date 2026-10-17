package dev.andrei.violaolivre.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.ui.metronome.MetronomeScreen
import dev.andrei.violaolivre.ui.metronome.MetronomeViewModel
import dev.andrei.violaolivre.ui.songs.SongsScreen
import dev.andrei.violaolivre.ui.theory.TheoryScreen
import dev.andrei.violaolivre.ui.tuner.TunerScreen

/** Estrutura principal: barra inferior com as abas + área que troca de tela. */
@Composable
fun AppNavHost() {
    // O NavController guarda a pilha de telas e sabe trocar entre elas
    val navController = rememberNavController()
    // Observa a tela atual; quando ela muda, a barra é redesenhada
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Criado aqui, fora do NavHost: o dono é a Activity, não a aba. Assim o metrônomo
    // continua tocando ao trocar de aba.
    val metronomeViewModel: MetronomeViewModel = viewModel()
    val metronomeState by metronomeViewModel.uiState.collectAsStateWithLifecycle()
    val metronomePlayingDescription = stringResource(R.string.metronome_playing)

    // Tela sempre acesa enquanto o metrônomo toca (senão ela apaga, o app sai de cena e o som para)
    val view = LocalView.current
    DisposableEffect(metronomeState.isPlaying) {
        view.keepScreenOn = metronomeState.isPlaying
        onDispose { view.keepScreenOn = false }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppDestination.entries.forEach { destination ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.hasRoute(destination.route::class) } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                // Evita empilhar abas: o "voltar" sempre leva à aba inicial
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Tocar na aba já aberta não cria uma cópia dela
                                launchSingleTop = true
                                // Ao voltar para uma aba, ela reaparece como estava
                                restoreState = true
                            }
                        },
                        icon = {
                            // Bolinha na aba do metrônomo quando ele está tocando em segundo plano
                            BadgedBox(
                                badge = {
                                    if (destination == AppDestination.METRONOME && metronomeState.isPlaying) {
                                        Badge(Modifier.semantics { contentDescription = metronomePlayingDescription })
                                    }
                                },
                            ) {
                                Icon(painterResource(destination.icon), contentDescription = null)
                            }
                        },
                        label = { Text(stringResource(destination.label)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        // innerPadding = espaço ocupado pela barra inferior e pela barra de status
        NavHost(
            navController = navController,
            startDestination = TunerRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<TunerRoute> { TunerScreen() }
            composable<SongsRoute> { SongsScreen() }
            composable<TheoryRoute> { TheoryScreen() }
            composable<MetronomeRoute> { MetronomeScreen(metronomeViewModel) }
        }
    }
}
