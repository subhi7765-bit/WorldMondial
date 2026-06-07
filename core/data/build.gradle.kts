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
    implementation(project(":core:analytics"))

    // Local Storage Jetpack DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Fixed Cleanly: Added Timber and Retrofit dependencies for BaseRepository core compilation
    implementation(libs.timber)
    implementation(libs.retrofit.core)

    // Fixed Cleanly: Added Firebase Crashlytics infrastructure required by BaseRepository error reporting
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-crashlytics")

    // Dagger-Hilt Dependency Injection Ecosystem with KSP Compiler
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
