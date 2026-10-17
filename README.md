# 🎸 Violão Livre

App Android pessoal para **afinar o violão**, **guardar e praticar tabs** e **aprender teoria musical**. Sem anúncios, sem assinatura, sem conta e funcionando offline.

> Projeto de estudo e uso pessoal: a ideia é aprender o fluxo completo de criar um app Android, do código até a instalação no meu próprio celular.

---

## Por que este projeto existe

Voltei a tocar violão e todos os apps de afinador e tabs que testei estavam cheios de anúncios ou com as funções boas atrás de pagamento. Em vez de continuar procurando, resolvi construir o meu, do jeito que eu uso.

---

## Funcionalidades

O app tem quatro abas na barra inferior:

### 1. Afinador
- Detecta a nota tocada pelo microfone e mostra a nota, a frequência (Hz) e o desvio em *cents*
- Ponteiro visual indicando se a corda está baixa, alta ou afinada
- Afinações prontas (tabela abaixo) e afinações personalizadas
- Seleção automática da corda mais próxima ou escolha manual
- Botão para ouvir a nota de referência de cada corda
- Modo cromático (qualquer nota)

### 2. Minhas músicas
- Lista das músicas com afinação e status de aprendizado (novo, aprendendo, dominada)
- Três formas de adicionar uma tab:
  - **Escrever** do zero num editor
  - **Colar** texto copiado de um site ou da descrição de um vídeo
  - **Importar** um arquivo `.txt`
- Cada música guarda: título, artista, afinação, capotraste, acordes usados, link do vídeo (ex.: tutorial do YouTube) e anotações
- Tela de leitura da tab com rolagem automática e velocidade ajustável
- Atalho da música direto para o afinador já na afinação dela
- Tocar no nome de um acorde abre o diagrama dele

### 3. Teoria
- Mapa interativo das notas no braço do violão
- Lições curtas: como ler tab, formação de acordes (maior, menor, 7ª), campo harmônico, escala pentatônica
- Diagramas de acordes

### 4. Metrônomo
- BPM ajustável, compasso (2/4, 3/4, 4/4, 6/8) e acento no primeiro tempo

---

## Afinações incluídas

Referência: A4 = 440 Hz. Cordas da 6ª (mais grave) para a 1ª (mais aguda).

| Afinação | 6ª | 5ª | 4ª | 3ª | 2ª | 1ª |
|---|---|---|---|---|---|---|
| Padrão | E2 | A2 | D3 | G3 | B3 | E4 |
| Meio tom abaixo | E♭2 | A♭2 | D♭3 | G♭3 | B♭3 | E♭4 |
| Um tom abaixo | D2 | G2 | C3 | F3 | A3 | D4 |
| Drop D | D2 | A2 | D3 | G3 | B3 | E4 |
| Drop C | C2 | G2 | C3 | F3 | A3 | D4 |
| DADGAD | D2 | A2 | D3 | G3 | A3 | D4 |
| Open G | D2 | G2 | D3 | G3 | B3 | D4 |
| Open D | D2 | A2 | D3 | F♯3 | A3 | D4 |
| Open E | E2 | B2 | E3 | G♯3 | B3 | E4 |

---

## Tecnologias

| Parte | Escolha |
|---|---|
| Linguagem | Kotlin |
| Interface | Jetpack Compose (Material 3) |
| Áudio | `AudioRecord` (captura) e `AudioTrack` (nota de referência e metrônomo) |
| Detecção de nota | Algoritmo YIN implementado no próprio projeto, sem bibliotecas externas |
| Banco de dados local | Room (SQLite) |
| Navegação | Navigation Compose |
| Build | Gradle (wrapper) |
| Editor | VS Code com Claude |

**Permissões:** apenas o microfone (`RECORD_AUDIO`), usado só enquanto o afinador está aberto. Nenhum dado sai do celular.

---

## Estrutura do projeto (planejada)

```
app/src/main/java/.../violaolivre/
├── MainActivity.kt
├── ui/
│   ├── navigation/      barra inferior e rotas
│   ├── tuner/           tela e ViewModel do afinador
│   ├── songs/           lista, editor e leitor de tabs
│   ├── theory/          lições e mapa do braço
│   ├── metronome/       tela do metrônomo
│   └── theme/           cores, tipografia, modo escuro
├── audio/
│   ├── PitchDetector.kt YIN: buffer de áudio → frequência
│   ├── MicListener.kt   captura contínua do microfone
│   └── ToneGenerator.kt gera a nota de referência
├── music/
│   ├── Note.kt          nota, oitava, frequência, cents
│   ├── Tuning.kt        afinações prontas e personalizadas
│   └── Chord.kt         acordes e diagramas
└── data/
    ├── SongEntity.kt
    ├── SongDao.kt
    └── AppDatabase.kt
```

---

## Roteiro

- [x] **Fase 0: Ambiente.** Instalar JDK e Android SDK, criar o projeto vazio e instalar no celular via cabo USB
- [ ] **Fase 1: Afinador.** Microfone, detecção de nota, afinações prontas e nota de referência
- [ ] **Fase 2: Músicas.** Banco local, lista, editor, colar e importar `.txt`, leitor com rolagem automática
- [ ] **Fase 3: Metrônomo**
- [ ] **Fase 4: Teoria.** Mapa do braço, diagramas de acordes e lições
- [ ] **Fase 5: Acabamento.** Modo escuro, ícone do app, backup e restauração das músicas em arquivo

---

## Como rodar no seu celular

### Pré-requisitos
- **JDK 17** ou mais recente
- **Android SDK** (o jeito mais simples é instalar o Android Studio só para baixar o SDK; o desenvolvimento acontece no VS Code)
- Celular Android com **Opções do desenvolvedor** e **Depuração USB** ativadas
  - Configurações → Sobre o telefone → toque 7 vezes em "Número da versão"
  - Configurações → Sistema → Opções do desenvolvedor → Depuração USB

### Compilar e instalar
Com o celular conectado por cabo USB e a depuração autorizada:

```bash
# conferir se o celular foi reconhecido
adb devices

# compilar e instalar a versão de desenvolvimento
./gradlew installDebug
```

No Windows, use `gradlew.bat installDebug`.

> **Sobre instalar fora da Play Store:** desde 30/09/2026, aparelhos Android certificados no Brasil exigem que apps instalados sejam de desenvolvedores verificados pelo Google. Apps não registrados continuam podendo ser instalados via **ADB** (cabo USB, o método acima), então para uso pessoal nada muda.

---

## Sobre as músicas

O app **não vem com músicas embutidas**. Letras e tabs de músicas publicadas têm direitos autorais, então cada usuário adiciona as suas, para estudo pessoal, e elas ficam guardadas só no aparelho.

---

## Projetos que serviram de referência

- [Tuner (moekadu)](https://f-droid.org/packages/de.moekadu.tuner/), afinador open source completo
- [StringSync](https://github.com/konliogka/stringsync), afinador em Java com algoritmo YIN
- [Tunerly](https://f-droid.org/packages/com.tunerly/), afinador com várias afinações
- [Tabs Lite](https://github.com/More-Than-Solitaire/Tabs-Lite), leitor de tabs sem anúncios
- [Artigo do algoritmo YIN](http://audition.ens.fr/adc/pdf/2002_JASA_YIN.pdf) (de Cheveigné e Kawahara, 2002)

---

## Licença

MIT. Use, modifique e aprenda à vontade.
