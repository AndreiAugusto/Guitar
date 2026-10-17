plugins {
    alias(libs.plugins.android.application)
    // Compilador do Compose: transforma as funções @Composable em código de interface
    alias(libs.plugins.kotlin.compose)
    // Usado pelas rotas tipadas do Navigation Compose
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.andrei.violaolivre"
    // Versão da API do Android usada para compilar (a mais recente)
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.andrei.violaolivre"
        // Android 8.0: versão mínima em que o app instala
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Saber quando o app inteiro vai para segundo plano (para o metrônomo parar)
    implementation(libs.androidx.lifecycle.process)

    // Ferramentas de @Preview: só entram na versão de desenvolvimento (debug)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Testes locais (rodam na JVM do computador, sem celular)
    testImplementation(libs.junit)
}
