plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "sa.mondial.world.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Aligned Kotlin bytecode execution to Java 17 compatibility under Gradle 8.9
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Internal Architectural Layer Dependencies
    implementation(project(":core:common"))

    // Type-Safe REST Remote Network Layer Client & Converters
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)

    // Crucial Fix: Added Kotlinx Serialization JSON library to resolve KSP NonExistentClass error
    implementation(libs.kotlinx.serialization.json)

    // Dagger-Hilt Dependency Injection Ecosystem with KSP Compiler
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
