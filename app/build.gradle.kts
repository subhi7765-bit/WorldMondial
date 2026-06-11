import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics") // Added Cleanly: Mandatory plugin definition to prevent instant runtime initialization crash
}

// Load local.properties safely for local development environments
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
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

    signingConfigs {
        create("release") {
            // Pointing to the keystore decoded by GitHub Actions
            storeFile = file("worldmondial.jks")
            // Fetching credentials securely from Environment Variables (GitHub Actions) or local.properties
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: localProperties.getProperty("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: localProperties.getProperty("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: localProperties.getProperty("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Apply the production release signing config
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Internal Modules Architecture 
    implementation(project(":core:common"))
    implementation(project(":core:di"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:analytics"))
    implementation(project(":core:notifications"))
    
    // Feature & Presentation Wiring
    implementation(project(":feature:matches"))
    implementation(project(":feature:news"))
    implementation(project(":feature:settings"))
    implementation(project(":core:sync"))

    // Foundational AndroidX AppCompat Library
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Jetpack Compose Libraries
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    
    // Dependency Injection & Logging
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.timber)
    
    // Hilt Navigation Compose Bridge
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    // Background Task Management WorkManager & Hilt Integration
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Core SplashScreen API library dependency
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.coil.svg)


    // Firebase Ecosystem Delivery Services (SECURED: Versions removed to let BOM handle them)
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
