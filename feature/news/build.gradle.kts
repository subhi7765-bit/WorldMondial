plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "sa.mondial.world.feature.news"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Explicitly aligned Kotlin compiler targets for Gradle 8.9 compatibility
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Internal Central Core Module Dependencies
    implementation(project(":core:common"))
    implementation(project(":core:di"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:analytics"))
    
    // Fixed Cleanly: Added explicit access to network and database modules to resolve NewsRepositoryImpl components via KSP
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    // AndroidX Jetpack Compose Core Libraries
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    // Modular Navigation Architecture Hooks
    implementation(libs.androidx.navigation.compose)
    
    // Enforced flat mapping catalog targets to ensure clean compilation under Gradle 8.9
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Dagger-Hilt Dependency Injection Ecosystem
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Serialization, Performance Image Loading, and Parsing Utilities
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)

    // Unified Test Architecture Framework
    testImplementation(project(":core:testing"))
}
