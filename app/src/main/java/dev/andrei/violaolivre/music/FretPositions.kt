package dev.andrei.violaolivre.music

/** Uma posição no braço: [string] 0 = 6ª corda (grave) ... 5 = 1ª (aguda); [fret] 0 = solta. */
data class FretPosition(val string: Int, val fret: Int) {
    fun note(tuning: Tuning = Tunings.STANDARD): Note = Note(tuning.notes[string].midi + fret)
}

object FretPositions {

    /** Todas as posições de 0 a [lastFret] onde [pitchClass] aparece. */
    fun of(pitchClass: Int, lastFret: Int = 12, tuning: Tuning = Tunings.STANDARD): List<FretPosition> =
        (0..5).flatMap { string ->
            (0..lastFret).map { FretPosition(string, it) }
        }.filter { it.note(tuning).pitchClass == pitchClass.mod(12) }

    /**
     * Um dos 5 "desenhos" (boxes) da pentatônica, com 2 notas por corda.
     *
     * O desenho [box] (0 a 4) começa na 6ª corda, no grau [box] da escala, e sobe
     * pegando as notas da escala em sequência: 2 na 6ª corda, as 2 seguintes na 5ª, etc.
     * Como as cordas estão a uma 4ª uma da outra, as notas ficam juntas num trecho de ~4 casas.
     */
    fun pentatonicBox(scale: Scale, box: Int, tuning: Tuning = Tunings.STANDARD): List<FretPosition> {
        require(scale.type == ScaleType.MINOR_PENTATONIC || scale.type == ScaleType.MAJOR_PENTATONIC)
        val openLow = tuning.notes[0].midi
        val startPitchClass = scale.pitchClasses[box]
        // Casa (0 a 11) da nota inicial na 6ª corda
        var startFret = (startPitchClass - openLow).mod(12)
        while (true) {
            val positions = boxFrom(scale, openLow + startFret, tuning)
            // Se alguma nota cairia "antes da casa 0", o desenho inteiro sobe uma oitava
            if (positions.all { it.fret >= 0 }) return positions
            startFret += 12
        }
    }

    private fun boxFrom(scale: Scale, startMidi: Int, tuning: Tuning): List<FretPosition> {
        // As 12 notas da escala a partir da inicial, em ordem crescente
        val notes = generateSequence(startMidi) { it + 1 }
            .filter { it.mod(12) in scale }
            .take(12)
            .toList()
        return notes.mapIndexed { i, midi ->
            val string = i / 2
            FretPosition(string, midi - tuning.notes[string].midi)
        }
    }
}
