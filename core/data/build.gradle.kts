plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "sa.mondial.world.core.data"
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
    // Internal Core Architectural Layer Dependencies
    implementation(project(":core:common"))
    implementation(project(":core:di"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    
    // Fixed Cleanly: Added analytics module access to resolve analyticsTracker in BaseRepository
    implementation(project(":core:analytics"))

    // Local Storage Jetpack DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Dagger-Hilt Dependency Injection Ecosystem with KSP Compiler
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
