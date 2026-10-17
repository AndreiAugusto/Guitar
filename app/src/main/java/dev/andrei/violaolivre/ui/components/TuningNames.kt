package dev.andrei.violaolivre.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.andrei.violaolivre.R
import dev.andrei.violaolivre.music.Tuning

/** Nome da afinação para exibir (vem de strings.xml, a partir do id). */
@Composable
fun tuningName(tuning: Tuning): String = stringResource(
    when (tuning.id) {
        "standard" -> R.string.tuning_standard
        "half_step_down" -> R.string.tuning_half_step_down
        "whole_step_down" -> R.string.tuning_whole_step_down
        "drop_d" -> R.string.tuning_drop_d
        "drop_c" -> R.string.tuning_drop_c
        "dadgad" -> R.string.tuning_dadgad
        "open_g" -> R.string.tuning_open_g
        "open_d" -> R.string.tuning_open_d
        "open_e" -> R.string.tuning_open_e
        else -> error("Afinação sem nome em strings.xml: ${tuning.id}")
    },
)

/** As notas das cordas sem a oitava, ex.: "D A D G B E". */
fun Tuning.stringNames(): String = notes.joinToString(" ") { it.name(useFlats) }
