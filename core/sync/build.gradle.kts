plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "sa.mondial.world.core.sync"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Core Architecture Modules Wiring
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database")) // Fixed Cleanly: Added the missing core database dependency mapping for KSP compile verification

    // Dependency Injection Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Background Task Management WorkManager Integration
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    
    // Logging framework
    implementation(libs.timber)
}
