plugins {
    // Usando alias (libs) que é a forma moderna e recomendada
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.devminds.casasync"
    compileSdk = 34 // Usar uma versão estável do SDK, como 34

    defaultConfig {
        applicationId = "com.devminds.casasync"
        minSdk = 26
        targetSdk = 34 // Manter o targetSdk igual ao compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        // A linha abaixo ativa o desugaring, precisa da dependência correspondente
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

// BLOCO DE DEPENDÊNCIAS CORRIGIDO E ESTABILIZADO
dependencies {

    // Dependências do AndroidX - Versões estáveis e compatíveis
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.biometric:biometric:1.1.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Firebase BoM (Bill of Materials) - Gerencia as versões do Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))

    // SDKs do Firebase (sem especificar a versão, a BoM cuida disso)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Google Play Services Auth (APENAS UMA VERSÃO)
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Dependência para o Core Library Desugaring (ESSENCIAL)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Testes
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}


