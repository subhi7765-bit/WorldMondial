plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "sa.mondial.world.core.database"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Aligned Kotlin compilation target for strict Gradle 8.9 configuration consistency
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Crucial Fix: Added common core architecture library to resolve DateParser and utilities successfully
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    // Local SQLite Object Relational Mapping Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Reactive Context Dependency Injection Ecosystem
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Standardized Multiplatform Parsing Utilities
    implementation(libs.kotlinx.serialization.json)
}
