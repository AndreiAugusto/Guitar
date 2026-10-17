package dev.andrei.violaolivre.ui.theory

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.ui.components.ScreenHeader
import dev.andrei.violaolivre.ui.navigation.FretboardNotesRoute

/** Um tópico da lista: título, subtítulo e a rota da tela dele. */
enum class TheoryTopic(@StringRes val title: Int, @StringRes val subtitle: Int, val route: Any) {
    FRETBOARD_NOTES(R.string.theory_fretboard_title, R.string.theory_fretboard_subtitle, FretboardNotesRoute),
}

@Composable
fun TheoryScreen(onOpenTopic: (route: Any) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        ScreenHeader(stringResource(R.string.tab_theory))
        // LazyColumn: lista que só desenha os itens visíveis (como um RecyclerView)
        LazyColumn(
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(TheoryTopic.entries) { topic ->
                Card(
                    onClick = { onOpenTopic(topic.route) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(topic.title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(topic.subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
