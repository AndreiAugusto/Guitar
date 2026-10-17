// Arquivo de build da raiz: só declara os plugins (com versão) sem aplicá-los.
// Cada módulo (aqui, só o :app) aplica os que precisa.
plugins {
    alias(libs.plugins.android.application) apply false
    // O AGP 9 já traz Kotlin embutido; declarar o plugin aqui fixa uma versão mais nova dele.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
