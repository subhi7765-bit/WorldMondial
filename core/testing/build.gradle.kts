plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "sa.mondial.world.core.testing"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Standardized Kotlin compilation parameters for the testing framework layer under Gradle 8.9
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Exposing core architecture testing utilities transitively using 'api' configuration
    api(libs.junit)
    api(libs.mockk)
    api(libs.turbine)
    api(libs.kotlinx.coroutines.test)
    
    // Resolved correctly from the updated version catalog successfully
    api(libs.androidx.test.rules)
    api(libs.androidx.test.runner)
    api(libs.androidx.test.core)
}
