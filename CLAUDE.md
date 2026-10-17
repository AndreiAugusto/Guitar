# CLAUDE.md — Violão Livre

App Android pessoal (afinador, tabs, teoria, metrônomo). A especificação completa e o roteiro estão no [README.md](README.md).

## Fase atual

**Fase 1 (Afinador)**: os 5 passos estão implementados e commitados; falta o usuário confirmar no celular
para marcar a fase no README. Afinações personalizadas ficaram para a Fase 2 (precisam do Room).
Concluídas: Fase 0 (app abre no celular e troca entre as 4 abas).

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
│   └── ToneGenerator.kt     toca a síntese com AudioTrack (MODE_STATIC)
├── music/
│   ├── Note.kt              nota por número MIDI, frequência (A4 = 440), nomes ♯/♭, centsBetween
│   └── Tuning.kt            Tuning (6 notas, 6ª → 1ª) e Tunings (as 9 do README)
└── ui/
    ├── navigation/          AppDestination (abas e rotas) e AppNavHost (Scaffold + NavigationBar + NavHost)
    ├── components/          composables reutilizáveis (PlaceholderScreen)
    ├── tuner/               TunerScreen (permissão + layout), TunerViewModel, TunerUiState (cálculos
    │                        testáveis: corda alvo, cents), TuningMeter (Canvas), TuningNames (id → strings.xml)
    ├── songs/  theory/  metronome/   telas provisórias
    └── theme/               Color.kt e Theme.kt
app/src/test/...             testes locais espelhando os pacotes acima
```

Notas do afinador:
- Microfone ligado só com a aba visível (`LifecycleStartEffect` na TunerScreen); a nota de referência também para ao sair.
- Constantes ajustáveis: `IN_TUNE_CENTS` (5) em TunerUiState, `SILENCE_WINDOWS` (~2,5 s) no ViewModel,
  `minRms`/`threshold` no PitchDetector.

Estrutura planejada para as próximas fases (`audio/`, `music/`, `data/`): ver README.
