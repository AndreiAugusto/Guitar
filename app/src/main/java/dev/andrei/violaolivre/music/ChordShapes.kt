package dev.andrei.violaolivre.music

/**
 * Um desenho de acorde no braço (afinação padrão).
 *
 * @param frets casa de cada corda, da 6ª para a 1ª: -1 = abafada (X), 0 = solta (O)
 * @param fingers dedo de cada corda (1 = indicador ... 4 = mínimo, 0 = nenhum)
 * @param barre casa da pestana (o indicador deitado sobre várias cordas), se houver
 * @param useFlats escrever o nome com bemol (ex.: B♭)
 */
data class ChordShape(
    val chord: Chord,
    val frets: List<Int>,
    val fingers: List<Int>,
    val barre: Int? = null,
    val useFlats: Boolean = false,
) {
    init {
        require(frets.size == 6 && fingers.size == 6) { "6 cordas: ${chord.name()}" }
    }

    val name: String get() = chord.name(useFlats)

    /** Primeira casa mostrada no diagrama: 1 para acordes abertos, ou a casa da pestana. */
    val baseFret: Int
        get() {
            val pressed = frets.filter { it > 0 }
            return if (pressed.isEmpty() || pressed.max() <= 4) 1 else pressed.min()
        }

    /** As notas que soam, da corda mais grave para a mais aguda. */
    fun notes(tuning: Tuning = Tunings.STANDARD): List<Note> =
        frets.mapIndexedNotNull { string, fret ->
            if (fret < 0) null else Note(tuning.notes[string].midi + fret)
        }
}

/** Biblioteca de desenhos de acordes mais usados no violão. */
object ChordShapes {

    // Atalho: "x32010" → casas; "032010" → dedos. Ex.: shape(C, MAJOR, "x32010", "032010")
    private fun shape(
        root: String,
        quality: ChordQuality,
        frets: String,
        fingers: String,
        barre: Int? = null,
    ): ChordShape {
        val pitchClass = Note.parse("${root}4").pitchClass
        return ChordShape(
            chord = Chord(pitchClass, quality),
            frets = frets.map { if (it == 'x') -1 else it.digitToInt(radix = 16) },
            fingers = fingers.map { it.digitToInt() },
            barre = barre,
            useFlats = root.contains('b') || root.contains('♭'),
        )
    }

    private val M = ChordQuality.MAJOR
    private val m = ChordQuality.MINOR
    private val DIM = ChordQuality.DIMINISHED
    private val D7 = ChordQuality.DOMINANT_7
    private val M7 = ChordQuality.MAJOR_7
    private val m7 = ChordQuality.MINOR_7

    // Casas acima de 9 usam letras (a = 10), para caber um caractere por corda
    val all: List<ChordShape> = listOf(
        // Maiores
        shape("C", M, "x32010", "032010"),
        shape("D", M, "xx0232", "000132"),
        shape("E", M, "022100", "023100"),
        shape("F", M, "133211", "134211", barre = 1),
        shape("G", M, "320003", "210003"),
        shape("A", M, "x02220", "001230"),
        shape("B", M, "x24442", "012341", barre = 2),
        shape("Bb", M, "x13331", "013331", barre = 1),
        // Menores
        shape("A", m, "x02210", "002310"),
        shape("E", m, "022000", "023000"),
        shape("D", m, "xx0231", "000231"),
        shape("B", m, "x24432", "013421", barre = 2),
        shape("F#", m, "244222", "134111", barre = 2),
        shape("C#", m, "x46654", "013421", barre = 4),
        shape("G#", m, "466444", "134111", barre = 4),
        shape("G", m, "355333", "134111", barre = 3),
        shape("C", m, "x35543", "013421", barre = 3),
        // Com 7ª (dominante)
        shape("A", D7, "x02020", "002030"),
        shape("B", D7, "x21202", "021304"),
        shape("C", D7, "x32310", "032410"),
        shape("D", D7, "xx0212", "000213"),
        shape("E", D7, "020100", "020100"),
        shape("G", D7, "320001", "320001"),
        // Com 7ª maior
        shape("C", M7, "x32000", "032000"),
        shape("D", M7, "xx0222", "000123"),
        shape("E", M7, "021100", "031200"),
        shape("F", M7, "xx3210", "003210"),
        shape("G", M7, "320002", "320001"),
        shape("A", M7, "x02120", "002130"),
        // Menores com 7ª
        shape("A", m7, "x02010", "002010"),
        shape("E", m7, "020000", "020000"),
        shape("D", m7, "xx0211", "000211"),
        shape("B", m7, "x20202", "020304"),
        // Diminutos (o vii° do campo harmônico)
        shape("B", DIM, "x2343x", "012430"),
        shape("C#", DIM, "x4565x", "012430"),
        shape("D#", DIM, "x6787x", "012430"),
        shape("E", DIM, "x7898x", "012430"),
        shape("F#", DIM, "2x421x", "204310"),
        shape("G#", DIM, "4x643x", "204310"),
    )

    /** O desenho de um acorde, se a biblioteca tiver. */
    fun forChord(chord: Chord): ChordShape? = all.firstOrNull { it.chord == chord }
}
