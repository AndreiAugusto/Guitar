# CLAUDE.md — Violão Livre

App Android pessoal (afinador, tabs, teoria, metrônomo). A especificação completa e o roteiro estão no [README.md](README.md).

## Fase atual

**Fase 0 (Ambiente)**: projeto criado e com barra inferior de 4 abas. Falta o usuário confirmar no celular.
Próxima: Fase 1 (Afinador).

## Como trabalhar com o usuário

- Ele programa, mas é novo em Kotlin/Android: explicar conceitos novos em poucas linhas, linguagem simples.
- **Uma fase por vez.** Só começar a próxima depois que ele confirmar que a atual funciona no celular.
- No início de cada fase: plano curto (arquivos a criar/mudar e por quê), esperar o ok dele.
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

Gradle 9.8.1 (wrapper) · AGP 9.4.1 · Kotlin 2.4.21 · compileSdk/targetSdk 37 · minSdk 26.
O AGP 9 tem Kotlin embutido (não aplicar `kotlin-android` no módulo); o plugin declarado com `apply false` na raiz só fixa a versão do Kotlin.

## Comandos

```bash
./gradlew assembleDebug    # compila o APK de debug
./gradlew installDebug     # compila e instala no celular conectado
adb devices                # lista aparelhos conectados
adb logcat                 # logs do celular
```

## Estrutura

```
app/src/main/java/dev/andrei/violaolivre/
├── MainActivity.kt          ponto de entrada; aplica o tema e o AppNavHost
└── ui/
    ├── navigation/          AppDestination (abas e rotas) e AppNavHost (Scaffold + NavigationBar + NavHost)
    ├── components/          composables reutilizáveis (PlaceholderScreen)
    ├── tuner/  songs/  theory/  metronome/   uma tela por aba (provisórias)
    └── theme/               Color.kt e Theme.kt
```

Estrutura planejada para as próximas fases (`audio/`, `music/`, `data/`): ver README.
