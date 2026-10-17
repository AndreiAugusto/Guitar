# CLAUDE.md — Violão Livre

App Android pessoal (afinador, tabs, teoria, metrônomo). A especificação completa e o roteiro estão no [README.md](README.md).

## Fase atual

**Fase 4 (Teoria)**: aguardando o ok do usuário para o plano.
Ordem combinada: 0 → 1 → 3 → 4 → 2 → 5 (Músicas ficou para depois, por escolha do usuário).
Concluídas: Fase 0 (ambiente + abas), Fase 1 (afinador) e Fase 3 (metrônomo com tap tempo), todas confirmadas no celular.
Pendências que dependem da Fase 2 (Room): afinações personalizadas; tocar no acorde de uma música abre o diagrama.

Ideias anotadas para depois: lição na Teoria sobre afinações e nomes das notas (já no roteiro da Fase 4 do README).

## Como trabalhar com o usuário

- O usuário programa, mas é novo em Kotlin/Android: explicar conceitos novos em poucas linhas, linguagem simples.
- **Uma fase por vez.** Só começar a próxima depois que o usuário confirmar que a atual funciona no celular.
- No início de cada fase: plano curto (arquivos a criar/mudar e por quê), esperar o ok do usuário.
- Depois de implementar: dizer exatamente como testar no celular e o que deve aparecer na tela.
- Commits pequenos, em português, ao fim de cada passo que funcionou. Marcar no README os itens concluídos (só após confirmação no celular).
- Se alguma escolha da stack parecer ruim, avisar antes de mudar.

## Convenções

- Explicações, textos da interface e comentários em **português do Brasil**; nomes de classes/funções/variáveis em inglês.
- Textos da interface sempre em `res/values/strings.xml` (nunca fixos no código).
- Versões de bibliotecas e plugins só em `gradle/libs.versions.toml`.
- Sem bibliotecas de áudio externas: detecção de nota com YIN implementado no projeto.
- Ícones: vector drawables em `res/drawable` (Material Symbols convertidos ou desenhados à mão), sem `material-icons-extended`.
- Navegação: Navigation Compose com rotas tipadas (`@Serializable` objects/classes).
- Tema: só escuro por enquanto (`ui/theme`), cores do mockup. Claro/escuro fica para a Fase 5.

## Ambiente (máquina do usuário)

- Ubuntu 26.04. JDK 21 em `/usr/lib/jvm/java-21-openjdk-amd64` (o Java 25 do sistema não é usado no build).
- Android SDK em `~/Android/Sdk`, instalado só com command-line tools (sem Android Studio).
- `JAVA_HOME`, `ANDROID_HOME` e `PATH` definidos no `~/.bashrc`. Em shells não interativos, exportar antes:
  `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ANDROID_HOME=$HOME/Android/Sdk`
- O `sdkmanager` está obsoleto: usar `android sdk install <pacote>` (ex.: `platforms/android-37.0`). Não precisa mais de `--licenses`.
- Celular: Samsung Galaxy M52 5G (Android 13 / API 33), instalação via ADB pelo cabo USB.

## Versões principais

Gradle 9.8.1 (wrapper) · AGP 9.4.1 · Kotlin 2.4.21 · compileSdk/targetSdk 37 · minSdk 26 · JUnit 4 nos testes locais.
O AGP 9 tem Kotlin embutido (não aplicar `kotlin-android` no módulo); o plugin declarado com `apply false` na raiz só fixa a versão do Kotlin.

## Comandos

```bash
./gradlew assembleDebug    # compila o APK de debug
./gradlew installDebug     # compila e instala no celular conectado
./gradlew testDebugUnitTest  # testes locais (rodam no computador)
./gradlew lintDebug        # análise estática (relatório em app/build/reports/)
adb devices                # lista aparelhos conectados
adb logcat                 # logs do celular
adb exec-out screencap -p > tela.png        # screenshot do celular (útil para conferir layout)
adb shell am start -n dev.andrei.violaolivre/.MainActivity
```

## Estrutura

```
app/src/main/java/dev/andrei/violaolivre/
├── MainActivity.kt          ponto de entrada; aplica o tema e o AppNavHost
├── audio/                   Kotlin puro testável, exceto MicListener e ToneGenerator
│   ├── PitchDetector.kt     YIN: janela de áudio (Float -1..1) → frequência + confiança
│   ├── PitchSmoother.kt     mediana das últimas leituras; salto de nota só com 2 leituras seguidas
│   ├── MicListener.kt       AudioRecord (44,1 kHz, PCM float) → Flow de janelas de 4096 com passo de 2048
│   ├── StringSynth.kt       síntese aditiva de corda dedilhada
│   ├── ToneGenerator.kt     toca a síntese com AudioTrack (MODE_STATIC)
│   ├── ClickSynth.kt        cliques do metrônomo (forte/médio/normal)
│   ├── ClickRenderer.kt     põe cada clique na amostra exata do tempo (relógio = contagem de amostras)
│   └── MetronomeEngine.kt   thread de áudio + AudioTrack MODE_STREAM; tempo soando via playbackHeadPosition
├── music/
│   ├── Note.kt              nota por número MIDI, frequência (A4 = 440), nomes ♯/♭, centsBetween
│   ├── Tuning.kt            Tuning (6 notas, 6ª → 1ª) e Tunings (as 9 do README)
│   ├── TimeSignature.kt     2/4, 3/4, 4/4, 6/8 (6/8 = colcheias, acento forte no 1 e leve no 4)
│   └── TapTempo.kt          BPM pela média dos últimos toques
└── ui/
    ├── navigation/          AppDestination (abas e rotas) e AppNavHost (Scaffold + NavigationBar + NavHost)
    ├── components/          composables reutilizáveis (PlaceholderScreen)
    ├── tuner/               TunerScreen (permissão + layout), TunerViewModel, TunerUiState (cálculos
    │                        testáveis: corda alvo, cents), TuningMeter (Canvas), TuningNames (id → strings.xml)
    ├── metronome/           MetronomeScreen e MetronomeViewModel (escopo da Activity, criado no AppNavHost)
    ├── songs/  theory/      telas provisórias
    └── theme/               Color.kt e Theme.kt
app/src/test/...             testes locais espelhando os pacotes acima
```

Notas do metrônomo:
- Continua tocando ao trocar de aba (ViewModel criado no AppNavHost, fora do NavHost); para quando o app
  vai para segundo plano (ProcessLifecycleOwner). Sem foreground service, por decisão do usuário.
- Enquanto toca: tela sempre acesa (keepScreenOn no AppNavHost) e bolinha (Badge) na aba Metrônomo.

Notas do afinador:
- Microfone ligado só com a aba visível (`LifecycleStartEffect` na TunerScreen); a nota de referência também para ao sair.
- Constantes ajustáveis: `IN_TUNE_CENTS` (5) em TunerUiState, `SILENCE_WINDOWS` (~2,5 s) no ViewModel,
  `minRms`/`threshold` no PitchDetector.

Estrutura planejada para as próximas fases (`audio/`, `music/`, `data/`): ver README.
