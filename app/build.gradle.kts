plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose") // Agregado según recomendación del error
}

android {
    namespace = "com.escom.UPush" // Asegurando que el namespace sea correcto
    compileSdk = 35

    defaultConfig {
        applicationId = "com.escom.UPush" // Asegurando que el applicationId sea correcto
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-functions")

    // AndroidX - Corrección de versión de Material
    implementation("androidx.core:core-ktx:1.12.0") // Versión más estable
    implementation("androidx.appcompat:appcompat:1.6.1") // Versión más estable
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Versión más estable
    implementation("com.google.android.material:material:1.10.0") // Versión disponible

    // Compose
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2") // Versión más estable
    implementation("androidx.activity:activity-compose:1.8.1") // Versión más estable
    implementation(platform("androidx.compose:compose-bom:2023.10.01")) // Versión más estable
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}