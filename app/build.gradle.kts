plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

android {
    namespace = "sa.mondial.world"
    compileSdk = 35

    defaultConfig {
        applicationId = "sa.mondial.world"
        minSdk = 26
        targetSdk = 35
        versionCode = 100
        versionName = "1.0.0"

        testInstrumentationRunner = "sa.mondial.world.core.testing.CustomTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs["debug"]
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Internal Core Layer Module Dependencies
    implementation(project(":core:common"))
    implementation(project(":core:di"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:analytics"))
    implementation(project(":core:notifications"))
    
    // Fixed cleanly from :project:sync to :core:sync to match settings structure
    implementation(project(":core:sync"))

    // Feature Presentation UI Module Dependencies
    implementation(project(":feature:matches"))
    implementation(project(":feature:news"))
    implementation(project(":feature:settings"))

    // AndroidX Jetpack Compose Presentation Core
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    // Jetpack Modular Navigation Ecosystem & Composition Bindings
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Dagger-Hilt Dependency Injection Compiler Ecosystem
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Production Logging, Serialization, and Background Task Management
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
    implementation(libs.androidx.work.runtime)

    // Firebase Centralized Analytics and Crash Reporting Infrastructure
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
