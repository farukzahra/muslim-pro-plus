import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Assinatura de release: local.properties manda (fica fora do git), env var serve
// de fallback para CI. O keystore em si vive em C:\repo\secrets\android e nunca
// entra no repo.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun releaseSecret(key: String, env: String): String? =
    (localProperties.getProperty(key) ?: System.getenv(env))?.takeIf { it.isNotBlank() }

val releaseKeystore = releaseSecret("muslimplus.keystore", "MUSLIMPLUS_KEYSTORE")
val releaseAlias = releaseSecret("muslimplus.alias", "MUSLIMPLUS_ALIAS")
val releaseStorePassword = releaseSecret("muslimplus.storePassword", "MUSLIMPLUS_STORE_PASSWORD")
val releaseKeyPassword = releaseSecret("muslimplus.keyPassword", "MUSLIMPLUS_KEY_PASSWORD")

val releaseKeystoreFile = releaseKeystore?.let { rootProject.file(it) }
val releaseSigningReady = releaseKeystoreFile != null &&
    releaseKeystoreFile.exists() &&
    !releaseAlias.isNullOrBlank() &&
    !releaseStorePassword.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

android {
    namespace = "com.farukzahra.muslimplus"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.farukzahra.muslimplus"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (releaseSigningReady) {
                storeFile = releaseKeystoreFile
                storePassword = releaseStorePassword
                this.keyAlias = releaseAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Sem keystore o release sai sem assinatura em vez de quebrar o
            // build todo; quem barra e o checkReleaseSigning abaixo.
            if (releaseSigningReady) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        // Sobre e Mais leem a versao de BuildConfig.VERSION_NAME; sem isto o
        // AGP 8 nao gera a classe e o numero volta a ficar hardcoded.
        buildConfig = true
    }
}

// assembleRelease/bundleRelease sem keystore falham aqui, com o que falta dito
// na mensagem. Debug, test e lint continuam funcionando em qualquer maquina.
val releaseSigningTasks = setOf("assembleRelease", "bundleRelease", "packageRelease")

tasks.matching { it.name in releaseSigningTasks }.configureEach {
    doFirst {
        if (!releaseSigningReady) {
            val missing = buildList {
                if (releaseKeystoreFile == null) add("muslimplus.keystore")
                else if (!releaseKeystoreFile.exists()) add("arquivo $releaseKeystoreFile")
                if (releaseAlias.isNullOrBlank()) add("muslimplus.alias")
                if (releaseStorePassword.isNullOrBlank()) add("muslimplus.storePassword")
                if (releaseKeyPassword.isNullOrBlank()) add("muslimplus.keyPassword")
            }
            throw GradleException(
                "Assinatura de release indisponivel: falta ${missing.joinToString(", ")}. " +
                    "Preencha local.properties (ou MUSLIMPLUS_KEYSTORE, MUSLIMPLUS_ALIAS, " +
                    "MUSLIMPLUS_STORE_PASSWORD, MUSLIMPLUS_KEY_PASSWORD). " +
                    "O keystore de upload fica em C:\\repo\\secrets\\android, nunca no repo.",
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.adhan)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Compose instrumentado: ui-test-junit4 vem do BOM; ui-test-manifest so
    // no debug, porque registra a activity vazia usada por createComposeRule.
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}
