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
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
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
    
    // Firebase Ecosystem Delivery Services
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    implementation("com.google.firebase:firebase-crashlytics:19.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
}
