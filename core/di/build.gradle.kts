plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "sa.mondial.world.core.di"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Core internal module dependencies
    implementation(project(":core:common"))

    // Dagger-Hilt Dependency Injection engine with KSP compiler processor
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Kotlin Coroutines core API needed to declare custom Dispatcher annotations cleanly
    implementation(libs.kotlinx.coroutines.core)
}
