plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "sa.mondial.world.feature.settings"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Explicitly aligned Kotlin compiler targets to ensure safe module compilation under Gradle 8.9
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Internal Architectural Layer Dependencies
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:di"))
    implementation(project(":core:ui"))

    // AndroidX Jetpack Compose Core Framework Components
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    // Dagger-Hilt Dependency Injection Ecosystem with KSP Compiler
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
}
