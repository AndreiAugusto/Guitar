package dev.andrei.violaolivre.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.music.Tuning
import dev.andrei.violaolivre.music.Tunings

/**
 * Botão com o nome da afinação que abre a lista das afinações prontas.
 * [selected] null = nenhuma marcada (ex.: modo cromático do afinador).
 * [extraItems] acrescenta itens no fim do menu; recebe uma função que fecha o menu.
 */
@Composable
fun TuningPicker(
    label: String,
    selected: Tuning?,
    onSelect: (Tuning) -> Unit,
    modifier: Modifier = Modifier,
    extraItems: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    // Box: o menu se posiciona em relação ao botão
    Box(modifier) {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    painterResource(R.drawable.ic_arrow_drop_down),
                    contentDescription = stringResource(R.string.tuner_choose_tuning),
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                trailingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            border = null,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Tunings.all.forEach { tuning ->
                TuningMenuItem(
                    title = tuningName(tuning),
                    subtitle = tuning.stringNames(),
                    selected = tuning == selected,
                    onClick = {
                        onSelect(tuning)
                        expanded = false
                    },
                )
            }
            extraItems { expanded = false }
        }
    }
}

/** Item do menu: nome em cima, detalhe embaixo (ex.: "Drop D" / "D A D G B E"). */
@Composable
fun TuningMenuItem(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Column {
                Text(
                    title,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        onClick = onClick,
    )
}
