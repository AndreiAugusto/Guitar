package dev.andrei.violaolivre

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.andrei.violaolivre.ui.navigation.AppNavHost
import dev.andrei.violaolivre.ui.theme.ViolaoLivreTheme

/** Ponto de entrada do app: o Android abre esta Activity ao tocar no ícone. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Desenha atrás das barras do sistema, com ícones claros (o app é escuro)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        // A partir daqui, a interface é descrita em Compose
        setContent {
            ViolaoLivreTheme {
                AppNavHost()
            }
        }
    }
}
